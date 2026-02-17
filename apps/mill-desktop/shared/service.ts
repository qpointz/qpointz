export type ServiceStatus = "stopped" | "starting" | "running" | "stopping" | "error";
export type ServiceType = "command" | "node" | "javaBoot" | "docker" | "docker-compose" | "python";

export interface ServiceLink {
  label: string;
  url: string;
}

export interface GracefulShutdownConfig {
  signal?: NodeJS.Signals;
  timeoutMs?: number;
}

export interface LifecyclePhaseConfig {
  [key: string]: any;
}

export interface ServiceLifecycleConfig {
  start?: LifecyclePhaseConfig;
  stop?: LifecyclePhaseConfig;
  clean?: LifecyclePhaseConfig;
}

export interface ServiceDefinition {
  id: string;
  name: string;
  description: string;
  logoPath?: string;
  readme?: string;
  serviceType: ServiceType;
  lifecycle?: ServiceLifecycleConfig;
  cwd?: string;
  env?: Record<string, string>;
  links: ServiceLink[];
  gracefulShutdown?: GracefulShutdownConfig;
}

export interface ServiceState {
  id: string;
  status: ServiceStatus;
  pid?: number;
  startedAt?: string;
  lastError?: string;
  prerequisitesOk?: boolean;
  prerequisitesMessage?: string;
  logFilePath: string;
}

export interface ServiceViewModel extends ServiceDefinition, ServiceState {}

export interface GeneralSettings {
  workspaceId: string;
  workspaceSlug: string;
}

export interface AiSettings {
  provider: string;
  apiKey: string;
  chatModel: string;
  embeddingModel: string;
}

export interface WorkspaceSettings {
  general: GeneralSettings;
  ai: AiSettings;
}

export interface WorkspaceInfo {
  directory: string;
  settings: WorkspaceSettings;
}

export interface RecentWorkspace {
  directory: string;
  settings: WorkspaceSettings;
}

export interface ControlPanelApi {
  listServices: () => Promise<ServiceViewModel[]>;
  startService: (serviceId: string) => Promise<ServiceViewModel>;
  stopService: (serviceId: string) => Promise<ServiceViewModel>;
  startAll: () => Promise<ServiceViewModel[]>;
  stopAll: () => Promise<ServiceViewModel[]>;
  openServiceLink: (serviceId: string, url: string) => Promise<void>;
  openLog: (serviceId: string) => Promise<void>;
  refreshStatuses: () => Promise<ServiceViewModel[]>;
  getWorkspace: () => Promise<WorkspaceInfo>;
  chooseWorkspace: () => Promise<WorkspaceInfo>;
  saveWorkspaceSettings: (settings: WorkspaceSettings) => Promise<WorkspaceInfo>;
  reloadWorkspaceServices: () => Promise<ServiceViewModel[]>;
  importWorkspaceServicesFromUrl: (url: string) => Promise<ServiceViewModel[]>;
  getRecentWorkspaces: () => Promise<RecentWorkspace[]>;
  switchToRecentWorkspace: (directory: string) => Promise<WorkspaceInfo>;
  deleteService: (serviceId: string) => Promise<ServiceViewModel[]>;
  cleanService: (serviceId: string) => Promise<ServiceViewModel>;
  getAppLogoUrl: () => Promise<string>;
  showConfirm: (message: string, detail?: string) => Promise<boolean>;
  showPrompt: (title: string, label: string) => Promise<string | null>;
}
