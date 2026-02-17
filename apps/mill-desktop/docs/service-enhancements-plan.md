---
name: Service settings and enhancements
overview: Add per-service settings (typed key-value triplets with UI hints), service directories, service cloning, and prerequisite auto-installation to mill-desktop.
todos:
  - id: settings-types
    content: Add ServiceSettingDef, PrerequisiteInstaller types and settings/prerequisites fields to ServiceDefinition in shared/service.ts
    status: pending
  - id: settings-backend
    content: Implement settings merge (manifest defaults + user overrides), persistence to .mill/service-settings/, and injection as MILL_SVC_* env vars
    status: pending
  - id: service-dir
    content: Create per-service directories at <workspace>/services/<slug>/, wire as default cwd and MILL_SVC_DIR env var
    status: pending
  - id: settings-ui
    content: Build ServiceSettingsView with dynamic form rendering (text, boolean, dir, file, service, list) and new IPC methods (browseForDir, browseForFile)
    status: pending
  - id: clone-backend
    content: "Implement cloneService in ServiceManager: deep-copy definition, apply name/description/settings overrides, generate new ID, append to manifest"
    status: pending
  - id: clone-ui
    content: Build CloneServiceView with editable name, description, and settings fields
    status: pending
  - id: prereq-install-handler
    content: Add getInstallInstructions() to handler interface, implement for node/java/docker handlers with download URLs
    status: pending
  - id: prereq-install-service
    content: Implement service-level prerequisite installer (url download + command execution) with progress in UI
    status: pending
  - id: prereq-install-ui
    content: Add Install button to ServiceCard when prerequisites are missing, wire to install flow
    status: pending
  - id: update-manifest
    content: Update fake services in services.manifest.json with example settings and prerequisites for testing
    status: pending
isProject: false
---

# Service-Level Settings, Directories, Cloning, and Prerequisite Installation

## Feature 1 — Service-Level Settings

### 1.1 Data model

Add a `ServiceSettingDef` type and a `settings` array to `ServiceDefinition` in [shared/service.ts](apps/mill-desktop/shared/service.ts):

```typescript
export type ServiceSettingType = "text" | "boolean" | "dir" | "file" | "service" | "list";

export interface ServiceSettingDef {
  key: string;
  name: string;
  description?: string;
  type: ServiceSettingType;
  value: string;                   // current value (stringified)
  listValues?: string[];           // only for type "list" — available options
}
```

On `ServiceDefinition`:

```typescript
export interface ServiceDefinition {
  // ... existing fields ...
  settings?: ServiceSettingDef[];  // NEW — declared settings with defaults
}
```

### 1.2 Manifest example

```json
{
  "id": "mill-grinder-ui",
  "name": "Fake Grinder Service",
  "settings": [
    { "key": "port", "name": "HTTP Port", "description": "Port for health endpoint", "type": "text", "value": "18080" },
    { "key": "verbose", "name": "Verbose logging", "type": "boolean", "value": "false" },
    { "key": "dataDir", "name": "Data directory", "type": "dir", "value": "" },
    { "key": "profile", "name": "Run profile", "type": "list", "value": "dev", "listValues": ["dev", "staging", "prod", "test"] },
    { "key": "upstream", "name": "Upstream service", "description": "Depends on this service", "type": "service", "value": "" }
  ]
}
```

### 1.3 Storage

- **Defaults** come from `services.manifest.json` (the `settings` array on each service definition).
- **User overrides** are persisted per-service in `<workspace>/.mill/service-settings/<service-id>.json`. Only changed values are stored; missing keys fall back to manifest defaults.
- On service load, `ServiceManager` merges manifest defaults with user overrides.

### 1.4 Injection into child processes

All resolved settings are injected as environment variables prefixed with `MILL_SVC_`:

```
MILL_SVC_PORT=18080
MILL_SVC_VERBOSE=false
MILL_SVC_DATADIR=C:\Users\vm\Downloads\services\fake-grinder\data
```

This extends the existing `buildOrchestrationEnv` in [ServiceLifecycleHandler.ts](apps/mill-desktop/electron/lifecycle/ServiceLifecycleHandler.ts).

### 1.5 UI — Service Settings view

- Add a **gear icon** on each `ServiceCard` that opens a per-service settings panel (full view, like workspace settings).
- Render settings dynamically based on `type`:
  - `text` — `TextInput`
  - `boolean` — `Switch`
  - `dir` — `TextInput` + "Browse" button (triggers `dialog.showOpenDialog` with `openDirectory`)
  - `file` — `TextInput` + "Browse" button (triggers `dialog.showOpenDialog` with `openFile`)
  - `service` — `Select` dropdown populated from current service list
  - `list` — `Select` dropdown populated from `listValues`
- Save button persists to `<workspace>/.mill/service-settings/<service-id>.json`.
- Show `name` as field label, `description` as helper text below field.

### 1.6 New IPC methods

```typescript
getServiceSettings(serviceId: string): Promise<ServiceSettingDef[]>;
saveServiceSettings(serviceId: string, settings: ServiceSettingDef[]): Promise<ServiceSettingDef[]>;
browseForDir(title: string): Promise<string | null>;
browseForFile(title: string): Promise<string | null>;
```

---

## Feature 2 — Service Directory

### 2.1 Concept

Each service gets its own isolated directory at `<workspace>/services/<service-slug>/`. This directory is:

- Created automatically when the service is first loaded.
- Used as the default `cwd` for the service process (overriding the current fallback to workspace root).
- Available to the service via `MILL_SVC_DIR` env var.
- A place for the service to store data, config, downloads, etc.

### 2.2 Implementation

- Add `serviceDir` (computed, not persisted) to `ServiceState` in [shared/service.ts](apps/mill-desktop/shared/service.ts).
- In `ServiceManager.loadServicesFromWorkspaceManifest()`, compute and `mkdirSync` each service dir.
- Slug derivation: reuse `toAlphaNumericSlug(definition.id)` (already exists in ServiceManager).
- Update `resolveCwd` in [ServiceLifecycleHandler.ts](apps/mill-desktop/electron/lifecycle/ServiceLifecycleHandler.ts): when no explicit `cwd` is configured, default to service dir instead of workspace root.
- Add `MILL_SVC_DIR` to `buildOrchestrationEnv`.
- Show the service dir path in `ServiceCard` and allow opening it via a folder icon button.

---

## Feature 3 — Service Cloning

### 3.1 UX flow

1. User clicks a **clone icon** on a service card.
2. A dialog/view opens pre-filled with:
   - **Name** (editable, defaulting to `"<original name> (copy)"`)
   - **Description** (editable, copied from original)
   - **Settings** (all service-level settings, editable)
3. ID is auto-generated from the new name slug + short random suffix.
4. Lifecycle config, service type, links, logo, graceful shutdown are **copied verbatim** from the original (not editable in the clone dialog).
5. On confirm, the new service definition is appended to the workspace manifest and its service dir is created.

### 3.2 Implementation

- New IPC: `cloneService(sourceId: string, overrides: { name: string; description: string; settings: ServiceSettingDef[] }): Promise<ServiceViewModel[]>`
- In `ServiceManager`: deep-copy the source definition, apply name/description/settings overrides, generate new ID, append to manifest, reload.
- UI: new `CloneServiceView` component (similar to the workspace settings view pattern).

---

## Feature 4 — Prerequisite Installation

### 4.1 Scope

Two levels of prerequisite installation:

- **Runtime prerequisites** — the handler's required tool (node, java, docker). Handler-specific install logic.
- **Service artifacts** — the service's own assets (JAR file, docker image, npm packages). Defined per-service.

### 4.2 Data model

Add to `ServiceDefinition`:

```typescript
export interface PrerequisiteInstaller {
  label: string;                   // e.g. "Download Node.js 20 LTS"
  type: "url" | "command";         // download a file or run a shell command
  url?: string;                    // for type "url": download target
  command?: string;                // for type "command": shell command to run
  args?: string[];
  targetPath?: string;             // where to save downloaded file (relative to service dir)
}

export interface ServiceDefinition {
  // ... existing fields ...
  prerequisites?: PrerequisiteInstaller[];
}
```

### 4.3 Handler-level runtime install

Extend `ServiceLifecycleHandler` interface:

```typescript
export interface ServiceLifecycleHandler {
  // ... existing ...
  getInstallInstructions(): { label: string; url: string } | null;
}
```

Each handler returns a download URL / instruction for its runtime:
- `node` -> link to nodejs.org
- `java` -> link to Adoptium JDK
- `docker` -> link to Docker Desktop
- `command` -> null (generic)

### 4.4 Service-level artifact install

When a service has `prerequisites` defined, show an **"Install"** button on the service card (visible when prereqs are missing or on demand). Clicking it:

1. For `type: "url"`: downloads the file to `targetPath` inside the service dir.
2. For `type: "command"`: spawns the command in the service dir (e.g. `npm install`, `docker pull`).
3. Shows progress/spinner in the UI.
4. Re-runs the handler `check()` after installation completes.

### 4.5 UI

- When `prerequisitesOk === false`, the service card shows:
  - The existing orange "Prereq missing" badge and message.
  - A new **"Install"** button that triggers the install flow.
  - If handler provides `getInstallInstructions()`, show a link "Install <runtime>" that opens the download page.
- If service has `prerequisites[]` entries, show an **"Install artifacts"** action in a card dropdown.

---

## Affected files summary

| Area | Files |
|------|-------|
| Types | [shared/service.ts](apps/mill-desktop/shared/service.ts) — `ServiceSettingDef`, `PrerequisiteInstaller`, `serviceDir` on state |
| Backend | [electron/main.ts](apps/mill-desktop/electron/main.ts) — `ServiceManager` settings merge, service dir creation, clone, install |
| Lifecycle | [electron/lifecycle/ServiceLifecycleHandler.ts](apps/mill-desktop/electron/lifecycle/ServiceLifecycleHandler.ts) — `MILL_SVC_*` env vars, `getInstallInstructions`, default cwd to service dir |
| Handlers | All 5 handler files — implement `getInstallInstructions()` |
| IPC | [electron/preload.ts](apps/mill-desktop/electron/preload.ts) — new methods |
| UI | [src/components/service/ServiceCard.tsx](apps/mill-desktop/src/components/service/ServiceCard.tsx) — gear, clone, install buttons, service dir link |
| UI (new) | `src/components/service/ServiceSettingsView.tsx` — dynamic settings form |
| UI (new) | `src/components/service/CloneServiceView.tsx` — clone dialog |
| Storage | `<workspace>/.mill/service-settings/<id>.json` — per-service setting overrides |
| Dirs | `<workspace>/services/<slug>/` — per-service working directory |
