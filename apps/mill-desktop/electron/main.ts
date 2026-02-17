import { app, BrowserWindow, dialog, ipcMain, net, protocol, shell, type MessageBoxSyncOptions } from "electron";
import { randomUUID } from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { pathToFileURL } from "node:url";
import yaml from "js-yaml";
import {
  ServiceDefinition,
  ServiceState,
  ServiceType,
  ServiceViewModel,
  GracefulShutdownConfig,
  WorkspaceInfo,
  WorkspaceSettings,
  RecentWorkspace
} from "../shared/service";
import { ServiceLifecycleFactory } from "./lifecycle/ServiceLifecycleFactory";
import {
  ServiceLifecycleContext,
  ServiceStartResult
} from "./lifecycle/ServiceLifecycleHandler";

const DEFAULT_TYPE_ICONS: Record<ServiceType, string> = {
  docker: "type-docker.svg",
  "docker-compose": "type-docker-compose.svg",
  javaBoot: "type-javaBoot.svg",
  node: "type-node.svg",
  python: "type-python.svg",
  command: "type-command.svg"
};

interface ManagedService {
  definition: ServiceDefinition;
  sourceDir: string;
  state: ServiceState;
  running?: ServiceStartResult;
}

class ServiceManager {
  private readonly services = new Map<string, ManagedService>();
  private readonly lifecycleFactory = new ServiceLifecycleFactory();
  private readonly lifecycleContext: ServiceLifecycleContext;
  private readonly defaultManifestPath: string;
  private workspaceDir: string;
  private workspaceSettings: WorkspaceSettings;

  constructor(
    private readonly resourcesDir: string,
    private readonly logDir: string,
    private readonly workspaceConfigPath: string,
    private readonly recentWorkspacesPath: string,
    private readonly defaultWorkspaceDir: string
  ) {
    this.defaultManifestPath = path.resolve(this.resourcesDir, "services.manifest.yaml");
    this.workspaceDir = this.loadWorkspaceDir();
    this.workspaceSettings = this.ensureWorkspaceSettings(this.workspaceDir);
    this.lifecycleContext = {
      resourcesDir: this.resourcesDir,
      resolveWorkspacePath: (candidate: string) => this.resolveWorkspacePath(candidate),
      resolveResourcePath: (candidate: string) => this.resolveResourcePath(candidate),
      getWorkspaceDir: () => this.workspaceDir,
      getWorkspaceSettingsPath: () => this.getWorkspaceSettingsPath(),
      getWorkspaceSettings: () => this.workspaceSettings,
      defaultShell: process.platform === "win32"
    };
    fs.mkdirSync(this.logDir, { recursive: true });
    this.bootstrapWorkspaceServiceFiles();
    this.loadServicesFromWorkspaceManifest();
    this.addRecentWorkspace(this.workspaceDir, this.workspaceSettings);
  }

  getWorkspace(): WorkspaceInfo {
    return { directory: this.workspaceDir, settings: this.workspaceSettings };
  }

  getRecentWorkspaces(): RecentWorkspace[] {
    return this.loadRecentWorkspaces();
  }

  async setWorkspace(directory: string): Promise<WorkspaceInfo> {
    if (!directory) {
      throw new Error("Workspace directory cannot be empty.");
    }

    const resolved = path.resolve(directory);
    if (!fs.existsSync(resolved) || !fs.statSync(resolved).isDirectory()) {
      throw new Error(`Workspace path is not a directory: ${resolved}`);
    }

    await this.stopAll();

    this.workspaceDir = resolved;
    this.persistWorkspaceDir(resolved);
    this.workspaceSettings = this.ensureWorkspaceSettings(this.workspaceDir);
    this.bootstrapWorkspaceServiceFiles();
    this.loadServicesFromWorkspaceManifest();
    this.addRecentWorkspace(this.workspaceDir, this.workspaceSettings);
    return this.getWorkspace();
  }

  async switchToRecentWorkspace(directory: string): Promise<WorkspaceInfo> {
    return this.setWorkspace(directory);
  }

  saveWorkspaceSettings(settings: WorkspaceSettings): WorkspaceInfo {
    const normalized = this.normalizeWorkspaceSettings(settings, this.workspaceSettings.general.workspaceId);
    this.workspaceSettings = normalized;
    this.persistWorkspaceSettings(this.workspaceDir, normalized);
    return this.getWorkspace();
  }

  async reloadWorkspaceServices(): Promise<ServiceViewModel[]> {
    await this.stopAll();
    this.bootstrapWorkspaceServiceFiles();
    this.loadServicesFromWorkspaceManifest();
    return this.listServices();
  }

  async importWorkspaceServicesFromUrl(url: string): Promise<ServiceViewModel[]> {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to download service definitions: HTTP ${response.status}`);
    }

    const payload = (await response.json()) as { services?: ServiceDefinition[] };
    const imported = payload.services ?? [];
    const filtered = imported.filter(
      (service) => service.serviceType === "docker" || service.serviceType === "docker-compose"
    );

    const workspaceManifestPath = this.getWorkspaceServiceManifestPath();
    const workspaceConfigDir = path.dirname(workspaceManifestPath);
    fs.mkdirSync(workspaceConfigDir, { recursive: true });
    fs.writeFileSync(workspaceManifestPath, yaml.dump({ services: filtered }, { lineWidth: -1 }), "utf-8");

    await this.stopAll();
    this.loadServicesFromWorkspaceManifest();
    return this.listServices();
  }

  private writeServiceLog(managed: ManagedService, tag: string, message: string): void {
    const ts = new Date().toISOString();
    const line = `[${ts}] [${tag}] ${message}\n`;
    try {
      fs.appendFileSync(managed.state.logFilePath, line, "utf-8");
    } catch { /* log dir might not exist yet */ }
  }

  async cleanService(id: string): Promise<ServiceViewModel> {
    const managed = this.mustGetService(id);
    if (managed.running) {
      managed.state.status = "stopping";
      this.writeServiceLog(managed, "CLEAN", "Stopping running service before clean...");
      await this.stopManagedService(managed);
    }
    this.writeServiceLog(managed, "CLEAN", "Running clean command...");
    const handler = this.lifecycleFactory.getHandler(managed.definition);
    const result = await handler.clean(managed.definition, managed.state.logFilePath, this.lifecycleContext);
    this.writeServiceLog(managed, "CLEAN", result.ran
      ? `Clean finished with exit code ${result.exitCode}`
      : "No clean command configured, skipped.");
    return this.toViewModel(managed);
  }

  async deleteService(id: string): Promise<ServiceViewModel[]> {
    const managed = this.mustGetService(id);
    this.writeServiceLog(managed, "DELETE", `Deleting service '${managed.definition.name}'...`);
    if (managed.running) {
      managed.state.status = "stopping";
      this.writeServiceLog(managed, "DELETE", "Stopping running service before deletion...");
      await this.stopManagedService(managed);
    }

    const handler = this.lifecycleFactory.getHandler(managed.definition);
    this.writeServiceLog(managed, "DELETE", "Running clean command before removal...");
    await handler.clean(managed.definition, managed.state.logFilePath, this.lifecycleContext);

    const manifestPath = this.getWorkspaceServiceManifestPath();
    const raw = fs.readFileSync(manifestPath, "utf-8");
    const parsed = yaml.load(raw) as { services?: ServiceDefinition[] };
    const filtered = (parsed.services ?? []).filter((service) => service.id !== id);
    fs.writeFileSync(manifestPath, yaml.dump({ services: filtered }, { lineWidth: -1 }), "utf-8");

    this.loadServicesFromWorkspaceManifest();
    return this.listServices();
  }

  hasRunningServices(): boolean {
    return [...this.services.values()].some((managed) => Boolean(managed.running));
  }

  listServices(): ServiceViewModel[] {
    this.refreshPrerequisites();
    return [...this.services.values()].map((managed) => ({
      ...this.toRendererDefinition(managed.definition, managed.sourceDir),
      ...managed.state
    }));
  }

  refreshStatuses(): ServiceViewModel[] {
    return this.listServices();
  }

  async startService(id: string): Promise<ServiceViewModel> {
    const managed = this.mustGetService(id);
    const currentStatus = managed.state.status;
    if (currentStatus === "running" || currentStatus === "starting") {
      return this.toViewModel(managed);
    }

    managed.state.status = "starting";
    managed.state.lastError = undefined;
    this.writeServiceLog(managed, "START", `Starting service '${managed.definition.name}'...`);

    const handler = this.lifecycleFactory.getHandler(managed.definition);
    const prerequisite = handler.check(managed.definition, this.lifecycleContext);
    managed.state.prerequisitesOk = prerequisite.ok;
    managed.state.prerequisitesMessage = prerequisite.message;
    if (!prerequisite.ok) {
      managed.state.status = "error";
      managed.state.lastError = prerequisite.message ?? "Prerequisite check failed.";
      this.writeServiceLog(managed, "ERROR", `Prerequisite check failed: ${managed.state.lastError}`);
      return this.toViewModel(managed);
    }

    try {
      const running = await handler.start(
        managed.definition,
        managed.state.logFilePath,
        this.lifecycleContext
      );
      managed.running = running;
      managed.state.pid = running.child.pid;
      managed.state.startedAt = new Date().toISOString();
      managed.state.status = "running";
      this.writeServiceLog(managed, "START", `Service started, PID=${running.child.pid}`);

      running.child.stdout.on("data", (chunk) => {
        running.logStream.write(`[stdout] ${chunk.toString()}`);
      });
      running.child.stderr.on("data", (chunk) => {
        running.logStream.write(`[stderr] ${chunk.toString()}`);
      });
      running.child.on("error", (error) => {
        managed.state.status = "error";
        managed.state.lastError = error.message;
        this.writeServiceLog(managed, "ERROR", `Process error: ${error.message}`);
      });
      running.child.on("close", (code, signal) => {
        const wasStopping = managed.state.status === "stopping";
        managed.running = undefined;
        managed.state.pid = undefined;
        managed.state.startedAt = undefined;
        running.logStream.end();
        if (wasStopping || code === 0) {
          managed.state.status = "stopped";
          managed.state.lastError = undefined;
          this.writeServiceLog(managed, "STOP", `Process exited normally (code=${code ?? "null"}, signal=${signal ?? "null"})`);
          return;
        }
        managed.state.status = "error";
        managed.state.lastError = `Exited with code=${code ?? "null"}, signal=${signal ?? "null"}`;
        this.writeServiceLog(managed, "ERROR", managed.state.lastError);
      });

      return this.toViewModel(managed);
    } catch (error) {
      managed.state.status = "error";
      managed.state.lastError = error instanceof Error ? error.message : String(error);
      this.writeServiceLog(managed, "ERROR", `Failed to start: ${managed.state.lastError}`);
      return this.toViewModel(managed);
    }
  }

  async stopService(id: string): Promise<ServiceViewModel> {
    const managed = this.mustGetService(id);
    if (!managed.running || managed.state.status === "stopped") {
      managed.state.status = "stopped";
      return this.toViewModel(managed);
    }

    managed.state.status = "stopping";
    this.writeServiceLog(managed, "STOP", `Stopping service '${managed.definition.name}'...`);
    await this.stopManagedService(managed);
    this.writeServiceLog(managed, "STOP", "Service stopped.");
    return this.toViewModel(managed);
  }

  async startAll(): Promise<ServiceViewModel[]> {
    for (const id of this.services.keys()) {
      await this.startService(id);
    }
    return this.listServices();
  }

  async stopAll(): Promise<ServiceViewModel[]> {
    for (const managed of this.services.values()) {
      if (managed.running) {
        managed.state.status = "stopping";
      }
    }
    await Promise.all([...this.services.values()].map((managed) => this.stopManagedService(managed)));
    return this.listServices();
  }

  async openLog(id: string): Promise<void> {
    const managed = this.mustGetService(id);
    const error = await shell.openPath(managed.state.logFilePath);
    if (error) {
      throw new Error(error);
    }
  }

  async openServiceLink(id: string, url: string): Promise<void> {
    if (id) {
      this.mustGetService(id);
    }
    await shell.openExternal(url);
  }

  private toViewModel(managed: ManagedService): ServiceViewModel {
    return { ...this.toRendererDefinition(managed.definition, managed.sourceDir), ...managed.state };
  }

  private resolveResourcePath(candidate: string): string {
    if (path.isAbsolute(candidate)) {
      return candidate;
    }
    return path.resolve(this.resourcesDir, candidate);
  }

  private resolveWorkspacePath(candidate: string): string {
    if (path.isAbsolute(candidate)) {
      return candidate;
    }
    if (!candidate.includes("/") && !candidate.includes("\\") && !candidate.startsWith(".")) {
      return candidate;
    }
    return path.resolve(this.workspaceDir, candidate);
  }

  private toRendererDefinition(definition: ServiceDefinition, sourceDir?: string): ServiceDefinition {
    const baseDir = sourceDir ?? this.resourcesDir;
    let absoluteLogo: string;
    if (definition.logoPath?.trim()) {
      absoluteLogo = this.resolveAssetPath(definition.logoPath, baseDir);
    } else {
      const defaultIcon = DEFAULT_TYPE_ICONS[definition.serviceType] ?? "type-default.svg";
      absoluteLogo = path.resolve(this.resourcesDir, "services", "assets", defaultIcon);
    }
    return {
      ...definition,
      logoPath: `${ASSET_PROTOCOL}://${encodeURIComponent(absoluteLogo)}`
    };
  }

  private resolveAssetPath(candidate: string, baseDir: string): string {
    if (path.isAbsolute(candidate)) {
      return candidate;
    }
    return path.resolve(baseDir, candidate);
  }

  private migrateLifecycle(definition: any): void {
    if (!definition.lifecycle) {
      definition.lifecycle = {};
    }
    const lc = definition.lifecycle;

    // migrate legacy top-level startCommand/args into lifecycle.start
    if (!lc.start && definition.startCommand) {
      lc.start = {
        command: definition.startCommand,
        args: definition.args ?? [],
        cwd: definition.cwd,
        env: definition.env,
        shell: definition.shell
      };
    }
    delete definition.startCommand;
    delete definition.args;
    delete definition.shell;

    // migrate v1 top-level lifecycle keys (lifecycle.command/node/javaBoot)
    if (!lc.start) {
      if (lc.command && typeof lc.command === "object") {
        lc.start = lc.command;
        delete lc.command;
      } else if (lc.node && typeof lc.node === "object") {
        lc.start = lc.node;
        delete lc.node;
      } else if (lc.javaBoot && typeof lc.javaBoot === "object") {
        lc.start = lc.javaBoot;
        delete lc.javaBoot;
      }
    }

    // migrate v2 nested start (lifecycle.start.command is an object, lifecycle.start.node, etc.)
    if (lc.start) {
      if (lc.start.command && typeof lc.start.command === "object") {
        lc.start = lc.start.command;
      } else if (lc.start.node && typeof lc.start.node === "object") {
        lc.start = lc.start.node;
      } else if (lc.start.javaBoot && typeof lc.start.javaBoot === "object") {
        lc.start = lc.start.javaBoot;
      }
    }

    // ensure serviceType defaults to "command"
    if (!definition.serviceType) {
      definition.serviceType = "command";
    }
  }

  private loadServicesFromWorkspaceManifest(): void {
    const manifestPath = this.getWorkspaceServiceManifestPath();
    if (!fs.existsSync(manifestPath)) {
      throw new Error(`Manifest not found: ${manifestPath}`);
    }

    const raw = fs.readFileSync(manifestPath, "utf-8");
    const parsed = yaml.load(raw) as { services?: any[] };
    const services = (parsed.services ?? []) as ServiceDefinition[];
    for (const def of services) { this.migrateLifecycle(def); }
    const sourceDir = path.dirname(manifestPath);

    this.services.clear();

    for (const definition of services) {
      const wsId = this.workspaceSettings.general.workspaceId;
      const logFilePath = path.resolve(this.logDir, `${wsId}_${definition.id}.log`);
      const initialState: ServiceState = {
        id: definition.id,
        status: "stopped",
        prerequisitesOk: undefined,
        prerequisitesMessage: undefined,
        logFilePath
      };
      this.services.set(definition.id, {
        definition,
        sourceDir,
        state: initialState
      });
    }

    this.refreshPrerequisites();
  }

  private async stopManagedService(managed: ManagedService): Promise<void> {
    if (!managed.running) {
      managed.state.status = "stopped";
      return;
    }

    const running = managed.running;
    const config = this.normalizeShutdownConfig(managed.definition.gracefulShutdown);
    const handler = this.lifecycleFactory.getHandler(managed.definition);
    await handler.stop(managed.definition, running, this.lifecycleContext, config);

    managed.running = undefined;
    managed.state.pid = undefined;
    managed.state.startedAt = undefined;
    managed.state.status = "stopped";
    managed.state.lastError = undefined;
    running.logStream.end();
  }

  private loadWorkspaceDir(): string {
    const fallback = this.defaultWorkspaceDir;
    fs.mkdirSync(fallback, { recursive: true });
    try {
      if (!fs.existsSync(this.workspaceConfigPath)) {
        return fallback;
      }
      const raw = fs.readFileSync(this.workspaceConfigPath, "utf-8");
      const parsed = JSON.parse(raw) as { directory?: string };
      const directory = parsed.directory ? path.resolve(parsed.directory) : fallback;
      if (!fs.existsSync(directory) || !fs.statSync(directory).isDirectory()) {
        return fallback;
      }
      return directory;
    } catch {
      return fallback;
    }
  }

  private ensureWorkspaceSettings(directory: string): WorkspaceSettings {
    const configDir = path.resolve(directory, ".mill");
    const settingsPath = path.resolve(configDir, "settings.json");
    fs.mkdirSync(configDir, { recursive: true });

    if (!fs.existsSync(settingsPath)) {
      const created = this.createDefaultWorkspaceSettings(directory);
      fs.writeFileSync(settingsPath, JSON.stringify(created, null, 2), "utf-8");
      return created;
    }

    try {
      const raw = fs.readFileSync(settingsPath, "utf-8");
      const parsed = JSON.parse(raw);
      const normalized = this.normalizeWorkspaceSettings(parsed, parsed?.general?.workspaceId);
      fs.writeFileSync(settingsPath, JSON.stringify(normalized, null, 2), "utf-8");
      return normalized;
    } catch {
      const fallback = this.createDefaultWorkspaceSettings(directory);
      fs.writeFileSync(settingsPath, JSON.stringify(fallback, null, 2), "utf-8");
      return fallback;
    }
  }

  private persistWorkspaceSettings(directory: string, settings: WorkspaceSettings): void {
    const configDir = path.resolve(directory, ".mill");
    const settingsPath = path.resolve(configDir, "settings.json");
    fs.mkdirSync(configDir, { recursive: true });
    fs.writeFileSync(settingsPath, JSON.stringify(settings, null, 2), "utf-8");
  }

  private getWorkspaceServiceManifestPath(): string {
    return path.resolve(this.workspaceDir, ".mill", "services.manifest.yaml");
  }

  private bootstrapWorkspaceServiceFiles(): void {
    const workspaceMillDir = path.resolve(this.workspaceDir, ".mill");
    const workspaceManifestPath = this.getWorkspaceServiceManifestPath();
    const defaultServicesDir = path.resolve(this.resourcesDir, "services");
    const workspaceServicesDir = path.resolve(workspaceMillDir, "services");

    fs.mkdirSync(workspaceMillDir, { recursive: true });
    if (!fs.existsSync(workspaceManifestPath)) {
      fs.copyFileSync(this.defaultManifestPath, workspaceManifestPath);
    }
    if (fs.existsSync(defaultServicesDir) && !fs.existsSync(workspaceServicesDir)) {
      fs.cpSync(defaultServicesDir, workspaceServicesDir, { recursive: true });
    }
  }

  private getWorkspaceSettingsPath(): string {
    return path.resolve(this.workspaceDir, ".mill", "settings.json");
  }

  private createDefaultWorkspaceSettings(directory: string): WorkspaceSettings {
    const dirName = path.basename(directory) || "workspace";
    const slug = this.toAlphaNumericSlug(dirName);
    return {
      general: {
        workspaceId: randomUUID(),
        workspaceSlug: slug || "workspace"
      },
      ai: {
        provider: "",
        apiKey: "",
        chatModel: "",
        embeddingModel: ""
      }
    };
  }

  private normalizeWorkspaceSettings(
    partial: any,
    fallbackId?: string
  ): WorkspaceSettings {
    const gen = partial?.general ?? partial ?? {};
    const ai = partial?.ai ?? {};
    const workspaceId = this.normalizeWorkspaceId(gen.workspaceId ?? fallbackId ?? randomUUID());
    const workspaceSlug = this.normalizeWorkspaceSlug(gen.workspaceSlug ?? "workspace");
    return {
      general: { workspaceId, workspaceSlug },
      ai: {
        provider: typeof ai.provider === "string" ? ai.provider : "",
        apiKey: typeof ai.apiKey === "string" ? ai.apiKey : "",
        chatModel: typeof ai.chatModel === "string" ? ai.chatModel : "",
        embeddingModel: typeof ai.embeddingModel === "string" ? ai.embeddingModel : ""
      }
    };
  }

  private normalizeWorkspaceId(value: string): string {
    const compact = value.replace(/[^a-zA-Z0-9]/g, "").toLowerCase();
    if (compact.length > 0) {
      return compact;
    }
    return randomUUID().replace(/[^a-zA-Z0-9]/g, "").toLowerCase();
  }

  private normalizeWorkspaceSlug(value: string): string {
    const slug = this.toAlphaNumericSlug(value);
    if (slug.length > 0) {
      return slug;
    }
    return "workspace";
  }

  private toAlphaNumericSlug(value: string): string {
    return value.replace(/[^a-zA-Z0-9]/g, "").toLowerCase();
  }

  private persistWorkspaceDir(directory: string): void {
    const parent = path.dirname(this.workspaceConfigPath);
    fs.mkdirSync(parent, { recursive: true });
    fs.writeFileSync(this.workspaceConfigPath, JSON.stringify({ directory }, null, 2), "utf-8");
  }

  private addRecentWorkspace(directory: string, settings: WorkspaceSettings): void {
    const current = this.loadRecentWorkspaces().filter((item) => path.resolve(item.directory) !== path.resolve(directory));
    const next: RecentWorkspace[] = [{ directory, settings }, ...current].slice(0, 12);
    this.persistRecentWorkspaces(next);
  }

  private loadRecentWorkspaces(): RecentWorkspace[] {
    try {
      if (!fs.existsSync(this.recentWorkspacesPath)) {
        return [];
      }
      const raw = fs.readFileSync(this.recentWorkspacesPath, "utf-8");
      const parsed = JSON.parse(raw) as { workspaces?: RecentWorkspace[] };
      const items = parsed.workspaces ?? [];
      return items.filter((item) => item?.directory && fs.existsSync(item.directory));
    } catch {
      return [];
    }
  }

  private persistRecentWorkspaces(workspaces: RecentWorkspace[]): void {
    const parent = path.dirname(this.recentWorkspacesPath);
    fs.mkdirSync(parent, { recursive: true });
    fs.writeFileSync(this.recentWorkspacesPath, JSON.stringify({ workspaces }, null, 2), "utf-8");
  }

  private normalizeShutdownConfig(config?: GracefulShutdownConfig): Required<GracefulShutdownConfig> {
    return {
      signal: config?.signal ?? "SIGTERM",
      timeoutMs: config?.timeoutMs ?? 6000
    };
  }

  private mustGetService(id: string): ManagedService {
    const managed = this.services.get(id);
    if (!managed) {
      throw new Error(`Unknown service: ${id}`);
    }
    return managed;
  }

  private refreshPrerequisites(): void {
    for (const managed of this.services.values()) {
      const st = managed.definition.serviceType ?? "command";
      if (!this.lifecycleFactory.isKnownType(st)) {
        managed.state.prerequisitesOk = false;
        managed.state.prerequisitesMessage = `Unknown service type '${st}'. Supported: command, node, javaBoot, docker, docker-compose, python`;
        continue;
      }
      const handler = this.lifecycleFactory.getHandler(managed.definition);
      const result = handler.check(managed.definition, this.lifecycleContext);
      managed.state.prerequisitesOk = result.ok;
      managed.state.prerequisitesMessage = result.message;
    }
  }
}

const ASSET_PROTOCOL = "mill-asset";

protocol.registerSchemesAsPrivileged([
  { scheme: ASSET_PROTOCOL, privileges: { bypassCSP: true, supportFetchAPI: true, stream: true } }
]);

const isDev = Boolean(process.env.VITE_DEV_SERVER_URL);
const appRoot = app.getAppPath();

function resolveDevResourcesDir(root: string): string {
  const candidates = [
    path.resolve(root, "resources"),
    path.resolve(root, "..", "resources"),
    path.resolve(root, "..", "..", "resources")
  ];
  for (const candidate of candidates) {
    if (fs.existsSync(path.resolve(candidate, "services.manifest.yaml"))) {
      return candidate;
    }
  }
  return path.resolve(root, "resources");
}

const resourcesDir = isDev ? resolveDevResourcesDir(appRoot) : path.resolve(process.resourcesPath, "resources");
const logDir = path.resolve(app.getPath("userData"), "logs");
const workspaceConfigPath = path.resolve(app.getPath("userData"), "workspace.json");
const recentWorkspacesPath = path.resolve(app.getPath("home"), ".mill", "recent-workspaces.json");
const defaultWorkspaceDir = isDev ? resourcesDir : path.resolve(app.getPath("userData"), "workspace");
const serviceManager = new ServiceManager(resourcesDir, logDir, workspaceConfigPath, recentWorkspacesPath, defaultWorkspaceDir);

function resolvePreloadPath(root: string): string {
  const candidates = [
    path.resolve(root, "dist-electron", "electron", "preload.js"),
    path.resolve(root, "preload.js"),
    path.resolve(root, "..", "preload.js"),
    path.resolve(root, "..", "dist-electron", "electron", "preload.js"),
    path.resolve(root, "..", "..", "dist-electron", "electron", "preload.js")
  ];

  for (const candidate of candidates) {
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  return path.resolve(root, "dist-electron", "electron", "preload.js");
}

let mainWindow: BrowserWindow | null = null;
let isShuttingDown = false;

function createMainWindow(): BrowserWindow {
  const window = new BrowserWindow({
    width: 1380,
    height: 900,
    minWidth: 1160,
    minHeight: 760,
    autoHideMenuBar: true,
    backgroundColor: "#f5f7fb",
    webPreferences: {
      // Use compiled main's directory for reliable preload resolution in dev and packaged modes.
      preload: path.resolve(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  });

  if (isDev && process.env.VITE_DEV_SERVER_URL) {
    window.loadURL(process.env.VITE_DEV_SERVER_URL).catch(console.error);
  } else {
    window.loadFile(path.resolve(appRoot, "dist", "index.html")).catch(console.error);
  }

  return window;
}

function registerIpcHandlers(): void {
  ipcMain.handle("cp:listServices", async () => serviceManager.listServices());
  ipcMain.handle("cp:refreshStatuses", async () => serviceManager.refreshStatuses());
  ipcMain.handle("cp:getWorkspace", async () => serviceManager.getWorkspace());
  ipcMain.handle("cp:getRecentWorkspaces", async () => serviceManager.getRecentWorkspaces());
  ipcMain.handle("cp:chooseWorkspace", async () => {
    const result = await dialog.showOpenDialog({
      title: "Select workspace directory",
      properties: ["openDirectory", "createDirectory"]
    });
    if (result.canceled || result.filePaths.length === 0) {
      return serviceManager.getWorkspace();
    }
    return serviceManager.setWorkspace(result.filePaths[0]!);
  });
  ipcMain.handle("cp:switchToRecentWorkspace", async (_event, directory: string) =>
    serviceManager.switchToRecentWorkspace(directory)
  );
  ipcMain.handle("cp:saveWorkspaceSettings", async (_event, settings: WorkspaceSettings) =>
    serviceManager.saveWorkspaceSettings(settings)
  );
  ipcMain.handle("cp:reloadWorkspaceServices", async () => serviceManager.reloadWorkspaceServices());
  ipcMain.handle("cp:importWorkspaceServicesFromUrl", async (_event, url: string) =>
    serviceManager.importWorkspaceServicesFromUrl(url)
  );
  ipcMain.handle("cp:startService", async (_event, serviceId: string) => serviceManager.startService(serviceId));
  ipcMain.handle("cp:stopService", async (_event, serviceId: string) => serviceManager.stopService(serviceId));
  ipcMain.handle("cp:deleteService", async (_event, serviceId: string) => serviceManager.deleteService(serviceId));
  ipcMain.handle("cp:cleanService", async (_event, serviceId: string) => serviceManager.cleanService(serviceId));
  ipcMain.handle("cp:startAll", async () => serviceManager.startAll());
  ipcMain.handle("cp:stopAll", async () => serviceManager.stopAll());
  ipcMain.handle("cp:openServiceLink", async (_event, serviceId: string, url: string) =>
    serviceManager.openServiceLink(serviceId, url)
  );
  ipcMain.handle("cp:openLog", async (_event, serviceId: string) => serviceManager.openLog(serviceId));
  ipcMain.handle("cp:getAppLogoUrl", () => {
    const logoPath = path.resolve(resourcesDir, "services", "assets", "mill-logo.png");
    return `${ASSET_PROTOCOL}://${encodeURIComponent(logoPath)}`;
  });

  ipcMain.handle("cp:showConfirm", async (_event, message: string, detail?: string) => {
    const options: MessageBoxSyncOptions = {
      type: "question",
      buttons: ["Cancel", "OK"],
      defaultId: 1,
      cancelId: 0,
      title: "Confirm",
      message,
      detail: detail ?? ""
    };
    const result = mainWindow
      ? dialog.showMessageBoxSync(mainWindow, options)
      : dialog.showMessageBoxSync(options);
    return result === 1;
  });

  ipcMain.handle("cp:showPrompt", async (_event, title: string, label: string) => {
    const { response, value } = await new Promise<{ response: number; value: string }>((resolve) => {
      const promptWindow = new BrowserWindow({
        width: 480,
        height: 200,
        parent: mainWindow ?? undefined,
        modal: true,
        resizable: false,
        minimizable: false,
        maximizable: false,
        autoHideMenuBar: true,
        webPreferences: { nodeIntegration: false, contextIsolation: true }
      });

      const html = `<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>${title}</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; padding: 24px; background: #1a1b1e; color: #c1c2c5; display: flex; flex-direction: column; height: calc(100vh - 48px); }
  label { font-size: 14px; margin-bottom: 8px; display: block; }
  input { width: 100%; box-sizing: border-box; padding: 8px 12px; border: 1px solid #373A40; border-radius: 4px; background: #25262b; color: #c1c2c5; font-size: 14px; outline: none; }
  input:focus { border-color: #1c7ed6; }
  .buttons { margin-top: auto; display: flex; justify-content: flex-end; gap: 8px; }
  button { padding: 6px 16px; border-radius: 4px; border: none; cursor: pointer; font-size: 14px; }
  .ok { background: #1c7ed6; color: #fff; }
  .cancel { background: #373A40; color: #c1c2c5; }
</style></head><body>
<label>${label}</label>
<input id="v" autofocus />
<div class="buttons">
  <button class="cancel" onclick="close(0)">Cancel</button>
  <button class="ok" onclick="close(1)">OK</button>
</div>
<script>
  const inp = document.getElementById('v');
  inp.addEventListener('keydown', e => { if (e.key === 'Enter') close(1); if (e.key === 'Escape') close(0); });
  function close(r) { document.title = JSON.stringify({ response: r, value: inp.value }); }
</script></body></html>`;

      promptWindow.loadURL(`data:text/html;charset=utf-8,${encodeURIComponent(html)}`);

      promptWindow.on("page-title-updated", (_e, newTitle) => {
        try {
          const parsed = JSON.parse(newTitle) as { response: number; value: string };
          promptWindow.close();
          resolve(parsed);
        } catch { /* ignore non-JSON title updates */ }
      });

      promptWindow.on("closed", () => {
        resolve({ response: 0, value: "" });
      });
    });

    return response === 1 && value.trim() ? value.trim() : null;
  });
}

async function gracefullyShutdownServices(): Promise<void> {
  if (isShuttingDown) {
    return;
  }
  isShuttingDown = true;
  await serviceManager.stopAll();
}

app.whenReady().then(() => {
  protocol.handle(ASSET_PROTOCOL, (request) => {
    const filePath = decodeURIComponent(request.url.slice(`${ASSET_PROTOCOL}://`.length));
    return net.fetch(pathToFileURL(filePath).href);
  });

  registerIpcHandlers();
  mainWindow = createMainWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      mainWindow = createMainWindow();
    }
  });
});

app.on("before-quit", (event) => {
  if (isShuttingDown) {
    return;
  }

  if (!serviceManager.hasRunningServices()) {
    isShuttingDown = true;
    return;
  }

  event.preventDefault();
  const promptOptions: MessageBoxSyncOptions = {
    type: "warning",
    buttons: ["Cancel", "Stop Services and Exit"],
    defaultId: 1,
    cancelId: 0,
    title: "Running Services",
    message: "Services are currently running.",
    detail: "All running services will be stopped before exit. Continue?"
  };

  const result = mainWindow
    ? dialog.showMessageBoxSync(mainWindow, promptOptions)
    : dialog.showMessageBoxSync(promptOptions);

  if (result !== 1) {
    return;
  }

  gracefullyShutdownServices()
    .catch(console.error)
    .finally(() => {
      app.quit();
    });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
