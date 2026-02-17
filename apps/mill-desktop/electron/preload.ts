import { contextBridge, ipcRenderer } from "electron";
import { ControlPanelApi, RecentWorkspace, ServiceViewModel, WorkspaceInfo, WorkspaceSettings } from "../shared/service";

const api: ControlPanelApi = {
  listServices: () => ipcRenderer.invoke("cp:listServices") as Promise<ServiceViewModel[]>,
  refreshStatuses: () => ipcRenderer.invoke("cp:refreshStatuses") as Promise<ServiceViewModel[]>,
  startService: (serviceId: string) => ipcRenderer.invoke("cp:startService", serviceId) as Promise<ServiceViewModel>,
  stopService: (serviceId: string) => ipcRenderer.invoke("cp:stopService", serviceId) as Promise<ServiceViewModel>,
  startAll: () => ipcRenderer.invoke("cp:startAll") as Promise<ServiceViewModel[]>,
  stopAll: () => ipcRenderer.invoke("cp:stopAll") as Promise<ServiceViewModel[]>,
  openServiceLink: (serviceId: string, url: string) => ipcRenderer.invoke("cp:openServiceLink", serviceId, url),
  openLog: (serviceId: string) => ipcRenderer.invoke("cp:openLog", serviceId),
  getWorkspace: () => ipcRenderer.invoke("cp:getWorkspace") as Promise<WorkspaceInfo>,
  chooseWorkspace: () => ipcRenderer.invoke("cp:chooseWorkspace") as Promise<WorkspaceInfo>,
  saveWorkspaceSettings: (settings: WorkspaceSettings) =>
    ipcRenderer.invoke("cp:saveWorkspaceSettings", settings) as Promise<WorkspaceInfo>,
  reloadWorkspaceServices: () => ipcRenderer.invoke("cp:reloadWorkspaceServices") as Promise<ServiceViewModel[]>,
  importWorkspaceServicesFromUrl: (url: string) =>
    ipcRenderer.invoke("cp:importWorkspaceServicesFromUrl", url) as Promise<ServiceViewModel[]>,
  getRecentWorkspaces: () => ipcRenderer.invoke("cp:getRecentWorkspaces") as Promise<RecentWorkspace[]>,
  switchToRecentWorkspace: (directory: string) =>
    ipcRenderer.invoke("cp:switchToRecentWorkspace", directory) as Promise<WorkspaceInfo>,
  deleteService: (serviceId: string) => ipcRenderer.invoke("cp:deleteService", serviceId) as Promise<ServiceViewModel[]>,
  cleanService: (serviceId: string) => ipcRenderer.invoke("cp:cleanService", serviceId) as Promise<ServiceViewModel>,
  getAppLogoUrl: () => ipcRenderer.invoke("cp:getAppLogoUrl") as Promise<string>,
  showConfirm: (message: string, detail?: string) => ipcRenderer.invoke("cp:showConfirm", message, detail) as Promise<boolean>,
  showPrompt: (title: string, label: string) => ipcRenderer.invoke("cp:showPrompt", title, label) as Promise<string | null>
};

contextBridge.exposeInMainWorld("controlPanel", api);
