import { ChildProcessWithoutNullStreams, spawn, spawnSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { GracefulShutdownConfig, LifecyclePhaseConfig, ServiceDefinition, ServiceType, WorkspaceSettings } from "../../shared/service";

export interface ServiceLifecycleContext {
  resourcesDir: string;
  resolveWorkspacePath: (candidate: string) => string;
  resolveResourcePath: (candidate: string) => string;
  getWorkspaceDir: () => string;
  getWorkspaceSettingsPath: () => string;
  getWorkspaceSettings: () => WorkspaceSettings;
  defaultShell: boolean;
}

export interface ServiceStartResult {
  child: ChildProcessWithoutNullStreams;
  logStream: fs.WriteStream;
}

export interface ServiceLifecycleHandler {
  readonly serviceType: ServiceType;
  check(definition: ServiceDefinition, context: ServiceLifecycleContext): { ok: boolean; message?: string };
  start(definition: ServiceDefinition, logFilePath: string, context: ServiceLifecycleContext): Promise<ServiceStartResult>;
  stop(
    definition: ServiceDefinition,
    running: ServiceStartResult,
    context: ServiceLifecycleContext,
    shutdown: Required<GracefulShutdownConfig>
  ): Promise<void>;
  clean(definition: ServiceDefinition, logFilePath: string, context: ServiceLifecycleContext): Promise<{ ran: boolean; exitCode: number | null }>;
}

export interface SpawnRequest {
  command: string;
  args: string[];
  cwd: string;
  env: NodeJS.ProcessEnv;
  shell: boolean;
}

export abstract class SpawnBasedLifecycleHandler implements ServiceLifecycleHandler {
  abstract readonly serviceType: ServiceType;

  protected abstract buildSpawnRequest(
    definition: ServiceDefinition,
    context: ServiceLifecycleContext
  ): SpawnRequest;

  check(_definition: ServiceDefinition, _context: ServiceLifecycleContext): { ok: boolean; message?: string } {
    return { ok: true };
  }

  async start(
    definition: ServiceDefinition,
    logFilePath: string,
    context: ServiceLifecycleContext
  ): Promise<ServiceStartResult> {
    const raw = this.buildSpawnRequest(definition, context);
    const request = this.applySubstitutions(raw, definition, logFilePath, context);
    const logStream = fs.createWriteStream(logFilePath, { flags: "a" });
    const child = spawn(request.command, request.args, {
      cwd: request.cwd,
      env: request.env,
      detached: false,
      shell: request.shell
    });
    return { child, logStream };
  }

  async stop(
    definition: ServiceDefinition,
    running: ServiceStartResult,
    context: ServiceLifecycleContext,
    shutdown: Required<GracefulShutdownConfig>
  ): Promise<void> {
    const stopConfig = definition.lifecycle?.stop;
    if (stopConfig) {
      await this.runStopCommand(definition, running, context, shutdown, stopConfig);
    } else {
      await this.signalStop(running, shutdown);
    }
  }

  private async signalStop(
    running: ServiceStartResult,
    shutdown: Required<GracefulShutdownConfig>
  ): Promise<void> {
    const { child } = running;
    await new Promise<void>((resolve) => {
      let completed = false;
      const finish = () => {
        if (completed) {
          return;
        }
        completed = true;
        resolve();
      };

      const timeout = setTimeout(() => {
        if (!completed) {
          child.kill("SIGKILL");
          finish();
        }
      }, shutdown.timeoutMs);

      child.once("exit", () => {
        clearTimeout(timeout);
        finish();
      });

      try {
        child.kill(shutdown.signal);
      } catch {
        clearTimeout(timeout);
        finish();
      }
    });
  }

  async clean(
    definition: ServiceDefinition,
    logFilePath: string,
    context: ServiceLifecycleContext
  ): Promise<{ ran: boolean; exitCode: number | null }> {
    const cleanConfig = definition.lifecycle?.clean;
    if (!cleanConfig) {
      return { ran: false, exitCode: null };
    }
    const logStream = fs.createWriteStream(logFilePath, { flags: "a" });
    try {
      const exitCode = await this.runPhaseCommand(cleanConfig, definition, logFilePath, context, logStream, "clean", 30000);
      return { ran: true, exitCode };
    } finally {
      logStream.end();
    }
  }

  private async runStopCommand(
    definition: ServiceDefinition,
    running: ServiceStartResult,
    context: ServiceLifecycleContext,
    shutdown: Required<GracefulShutdownConfig>,
    stopConfig: LifecyclePhaseConfig
  ): Promise<void> {
    const { child } = running;
    await this.runPhaseCommand(stopConfig, definition, running.logStream.path as string, context, running.logStream, "stop", shutdown.timeoutMs);
    if (!child.killed && child.exitCode === null) {
      try { child.kill("SIGKILL"); } catch { /* already exited */ }
    }
  }

  protected runPhaseCommand(
    config: LifecyclePhaseConfig,
    definition: ServiceDefinition,
    logFilePath: string,
    context: ServiceLifecycleContext,
    logStream: fs.WriteStream,
    phase: string,
    timeoutMs: number
  ): Promise<number | null> {
    const command = (config.command as string) ?? "echo";
    const args = (config.args as string[]) ?? [];
    const cwd = this.resolveCwd(definition, context, config.cwd as string | undefined);
    const env = {
      ...process.env,
      ...this.buildOrchestrationEnv(context),
      ...definition.env,
      ...(config.env as Record<string, string> | undefined)
    } as NodeJS.ProcessEnv;
    const useShell = (config.shell as boolean | undefined) ?? context.defaultShell;

    const wsVars = this.buildWorkspaceVars(context);
    const svcVars = this.buildServiceVars(definition, logFilePath, context);

    substituteAndValidate(
      `${phase} command for '${definition.name}'`,
      { command, args, cwd, env: env as Record<string, string | undefined> },
      wsVars,
      svcVars
    );

    const s = (v: string) => subst(v, wsVars, svcVars);
    const child = spawn(s(command), args.map(s), {
      cwd: s(cwd),
      env: Object.fromEntries(
        Object.entries(env).map(([k, v]) => [k, v != null ? s(String(v)) : v])
      ) as NodeJS.ProcessEnv,
      detached: false,
      shell: useShell
    });

    child.stdout?.on("data", (d: Buffer) => logStream.write(`[${phase}:stdout] ${d}`));
    child.stderr?.on("data", (d: Buffer) => logStream.write(`[${phase}:stderr] ${d}`));

    return new Promise<number | null>((resolve) => {
      let completed = false;
      const finish = (code: number | null) => {
        if (completed) return;
        completed = true;
        resolve(code);
      };

      const timeout = setTimeout(() => {
        if (!completed) {
          child.kill("SIGKILL");
          finish(null);
        }
      }, timeoutMs);

      child.once("exit", (code) => {
        clearTimeout(timeout);
        finish(code);
      });
    });
  }

  protected applySubstitutions(
    request: SpawnRequest,
    definition: ServiceDefinition,
    logFilePath: string,
    context: ServiceLifecycleContext
  ): SpawnRequest {
    const wsVars = this.buildWorkspaceVars(context);
    const svcVars = this.buildServiceVars(definition, logFilePath, context);

    substituteAndValidate(
      `service '${definition.name}'`,
      {
        command: request.command,
        args: request.args,
        cwd: request.cwd,
        env: request.env as Record<string, string | undefined>
      },
      wsVars,
      svcVars
    );

    const s = (v: string) => subst(v, wsVars, svcVars);
    return {
      command: s(request.command),
      args: request.args.map(s),
      cwd: s(request.cwd),
      env: Object.fromEntries(
        Object.entries(request.env).map(([k, v]) => [k, v != null ? s(String(v)) : v])
      ) as NodeJS.ProcessEnv,
      shell: request.shell
    };
  }

  protected buildWorkspaceVars(context: ServiceLifecycleContext): Record<string, string> {
    const settings = context.getWorkspaceSettings();
    const vars: Record<string, string> = {
      dir: context.getWorkspaceDir(),
      settingsPath: context.getWorkspaceSettingsPath()
    };
    const flatten = (obj: Record<string, any>, prefix: string) => {
      for (const [key, value] of Object.entries(obj)) {
        const dotKey = prefix ? `${prefix}.${key}` : key;
        if (typeof value === "string") {
          vars[dotKey] = value;
        } else if (value && typeof value === "object" && !Array.isArray(value)) {
          flatten(value as Record<string, any>, dotKey);
        }
      }
    };
    flatten(settings as unknown as Record<string, any>, "");
    if (settings.general) {
      vars["workspaceId"] = settings.general.workspaceId;
      vars["workspaceSlug"] = settings.general.workspaceSlug;
    }
    return vars;
  }

  protected buildServiceVars(
    definition: ServiceDefinition,
    logFilePath: string,
    context: ServiceLifecycleContext
  ): Record<string, string> {
    return {
      id: definition.id,
      name: definition.name,
      description: definition.description,
      type: definition.serviceType ?? "command",
      logPath: logFilePath,
      dir: path.resolve(context.getWorkspaceDir(), "services", definition.id)
    };
  }

  protected resolveCwd(definition: ServiceDefinition, context: ServiceLifecycleContext, override?: string): string {
    const candidate = override ?? definition.cwd;
    if (!candidate) {
      return context.getWorkspaceDir();
    }
    return context.resolveWorkspacePath(candidate);
  }

  protected buildOrchestrationEnv(context: ServiceLifecycleContext): Record<string, string> {
    const settings = context.getWorkspaceSettings();
    return {
      MILL_WORKSPACE_DIR: context.getWorkspaceDir(),
      MILL_WORKSPACE_SETTINGS_PATH: context.getWorkspaceSettingsPath(),
      MILL_WS_GENERAL_WORKSPACE_ID: settings.general.workspaceId,
      MILL_WS_GENERAL_WORKSPACE_SLUG: settings.general.workspaceSlug,
      MILL_WS_AI_PROVIDER: settings.ai.provider,
      MILL_WS_AI_API_KEY: settings.ai.apiKey,
      MILL_WS_AI_CHAT_MODEL: settings.ai.chatModel,
      MILL_WS_AI_EMBEDDING_MODEL: settings.ai.embeddingModel,
      MILL_WORKSPACE_SETTINGS_JSON: JSON.stringify(settings)
    };
  }

  protected commandExists(command: string, args: string[] = ["--version"]): boolean {
    const probe = spawnSync(command, args, {
      stdio: "ignore",
      shell: process.platform === "win32"
    });
    return probe.status === 0;
  }

  protected commandResolvable(command: string): boolean {
    if (path.isAbsolute(command) || command.includes("/") || command.includes("\\") || command.startsWith(".")) {
      return fs.existsSync(command);
    }
    const locator = process.platform === "win32" ? "where" : "which";
    const probe = spawnSync(locator, [command], {
      stdio: "ignore",
      shell: process.platform === "win32"
    });
    return probe.status === 0;
  }
}

const SUBSTITUTION_PATTERN = /\[\[(ws|svc):([^\]]+)\]\]/g;

interface SubstitutionResult {
  value: string;
  unresolved: string[];
}

function substituteVars(
  input: string,
  wsVars: Record<string, string>,
  svcVars: Record<string, string>
): SubstitutionResult {
  const unresolved: string[] = [];
  const value = input.replace(SUBSTITUTION_PATTERN, (match, ns: string, key: string) => {
    const vars = ns === "svc" ? svcVars : wsVars;
    if (key in vars) {
      return vars[key];
    }
    unresolved.push(match);
    return match;
  });
  return { value, unresolved };
}

function subst(s: string, wsVars: Record<string, string>, svcVars: Record<string, string>): string {
  return substituteVars(s, wsVars, svcVars).value;
}

function substituteAndValidate(
  label: string,
  fields: Record<string, string | string[] | Record<string, string | undefined>>,
  wsVars: Record<string, string>,
  svcVars: Record<string, string>
): void {
  const allUnresolved: string[] = [];
  const check = (s: string) => {
    const r = substituteVars(s, wsVars, svcVars);
    allUnresolved.push(...r.unresolved);
  };
  for (const [, val] of Object.entries(fields)) {
    if (typeof val === "string") {
      check(val);
    } else if (Array.isArray(val)) {
      val.forEach(check);
    } else if (val && typeof val === "object") {
      for (const v of Object.values(val)) {
        if (typeof v === "string") check(v);
      }
    }
  }
  if (allUnresolved.length > 0) {
    const unique = [...new Set(allUnresolved)];
    throw new Error(`Unresolved placeholders in ${label}: ${unique.join(", ")}`);
  }
}
