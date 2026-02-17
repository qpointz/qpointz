import { ControlPanelApi } from "../../shared/service";

declare global {
  interface Window {
    controlPanel: ControlPanelApi;
  }
}

export {};
