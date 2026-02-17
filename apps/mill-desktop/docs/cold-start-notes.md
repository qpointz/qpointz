# Mill Desktop Cold Start Notes

This document captures the full "cold start" implementation context for `apps/mill-desktop`, including product intent, architecture decisions, runtime behavior, workspace model, and day-1 operational guidance.

## 1) Product intent and scope

The app is a standalone Electron desktop control panel that:

- runs cross-platform
- launches bundled/local services as child processes
- displays service metadata (logo, name, description, links)
- shows service health status by lamp state (red/green/gray + transitional states)
- offers per-service log access
- guarantees child process cleanup on app shutdown

The implementation is intentionally independent from `ui/mill-ui` code. It clones only visual style direction (Mantine shell, spacing, accents).

## 2) Current module and key paths

- App module: `apps/mill-desktop`
- Electron main process: `apps/mill-desktop/electron/main.ts`
- IPC bridge: `apps/mill-desktop/electron/preload.ts`
- Lifecycle handlers:
  - `apps/mill-desktop/electron/lifecycle/ServiceLifecycleHandler.ts` — base interface, abstract spawn class, utility methods
  - `apps/mill-desktop/electron/lifecycle/ServiceLifecycleFactory.ts` — maps service types to handler instances
  - `apps/mill-desktop/electron/lifecycle/handlers/CommandServiceLifecycleHandler.ts`
  - `apps/mill-desktop/electron/lifecycle/handlers/NodeServiceLifecycleHandler.ts`
  - `apps/mill-desktop/electron/lifecycle/handlers/JavaBootServiceLifecycleHandler.ts`
  - `apps/mill-desktop/electron/lifecycle/handlers/DockerServiceLifecycleHandler.ts`
  - `apps/mill-desktop/electron/lifecycle/handlers/DockerComposeServiceLifecycleHandler.ts`
- Shared contracts: `apps/mill-desktop/shared/service.ts`
- Renderer root: `apps/mill-desktop/src/App.tsx`
- Header UI: `apps/mill-desktop/src/components/layout/AppHeader.tsx`
- Service card UI: `apps/mill-desktop/src/components/service/ServiceCard.tsx`
- Service manifest: `apps/mill-desktop/resources/services.manifest.yaml`
- Fake service launcher: `apps/mill-desktop/resources/services/bin/fake-service.js`
- Bundled logo: `apps/mill-desktop/resources/services/assets/mill-logo.png`

## 3) Service runtime model

### 3.1 Child-process orchestration

All services are launched from Electron main as tracked children. Current states:

- `stopped`
- `starting`
- `running`
- `stopping`
- `error`

Shutdown behavior:

- app quit triggers service stop workflow
- stop attempts graceful signal first
- timeout fallback uses `SIGKILL`

### 3.2 Service types and handler strategy

Lifecycle logic is split by service type via the strategy pattern. Each type has a dedicated handler class extending `SpawnBasedLifecycleHandler`:

| Service type     | Handler class                          | Prerequisite check            |
|------------------|----------------------------------------|-------------------------------|
| `command`        | `CommandServiceLifecycleHandler`       | configured command resolvable |
| `node`           | `NodeServiceLifecycleHandler`          | `node --version`              |
| `javaBoot`       | `JavaBootServiceLifecycleHandler`      | `java --version`              |
| `docker`         | `DockerServiceLifecycleHandler`        | `docker --version`            |
| `docker-compose` | `DockerComposeServiceLifecycleHandler` | `docker compose version` or `docker-compose --version` |

`ServiceLifecycleFactory` maps service type strings to handler instances. The factory also infers type from lifecycle config when `serviceType` is unset.

### 3.3 Prerequisite checks

Every handler implements `check(definition, context) => { ok, message? }`.

- Checks run synchronously using `spawnSync` to probe for required CLI tools.
- `SpawnBasedLifecycleHandler` provides two utility methods:
  - `commandExists(command, args)` — spawns the command and checks for exit code 0.
  - `commandResolvable(command)` — for path-like commands checks `fs.existsSync`; for bare names uses `where` (Windows) or `which` (Unix).
- Prerequisites are refreshed every time services are listed.
- `startService` re-checks prerequisites and blocks launch with a clear error when the check fails.
- The `ServiceState` carries `prerequisitesOk` and `prerequisitesMessage` so the renderer can display the status.

### 3.4 Docker and Docker Compose specifics

**DockerServiceLifecycleHandler**:
- Defaults command to `docker` when no explicit command is provided.
- Uses command lifecycle config (`definition.lifecycle.command`) for args, cwd, env, shell.

**DockerComposeServiceLifecycleHandler**:
- Check probes `docker compose version` first (v2 plugin), then falls back to `docker-compose --version` (v1 standalone).
- When no explicit start command is configured, defaults to `docker compose <args>`.
- When a custom command is provided (e.g. `docker-compose`), uses it directly with the configured args.

## 4) Workspace model

Workspace is first-class.

### 4.1 What a workspace is

- user-selected local directory
- must contain (or will be provisioned with) `.mill/settings.json`

### 4.2 Persistence

- selected workspace path is persisted in user data config (`workspace.json`)
- workspace settings are persisted in `<workspace>/.mill/settings.json`

### 4.3 Workspace settings schema (current)

`WorkspaceSettings` — grouped structure:

- `general`
  - `workspaceId` (normalized alphanumeric)
  - `workspaceSlug` (normalized alphanumeric lowercase)
- `ai`
  - `provider` (string)
  - `apiKey` (string)
  - `chatModel` (string)
  - `embeddingModel` (string)

### 4.4 Workspace switching behavior

When workspace is changed:

- all running services are stopped first
- new workspace is set
- `.mill/settings.json` is ensured/normalized for new workspace

### 4.5 Recent workspaces

- Stored in `~/.mill/recent-workspaces.json` (user home directory).
- Up to 12 entries kept, pruned by existence check on load.
- Accessible via Workspace dropdown menu in the header.

## 5) Launcher access to workspace settings

All service launchers receive workspace orchestration context via env vars:

- `MILL_WORKSPACE_DIR`
- `MILL_WORKSPACE_SETTINGS_PATH`
- `MILL_WS_GENERAL_WORKSPACE_ID`
- `MILL_WS_GENERAL_WORKSPACE_SLUG`
- `MILL_WS_AI_PROVIDER`
- `MILL_WS_AI_API_KEY`
- `MILL_WS_AI_CHAT_MODEL`
- `MILL_WS_AI_EMBEDDING_MODEL`
- `MILL_WORKSPACE_SETTINGS_JSON`

This enables launchers to use workspace settings for orchestration logic without extra IPC calls.

## 6) Asset protocol and logo serving

### 6.1 Problem

Electron's sandboxed renderer blocks `file://` protocol URLs, so service logos and the app logo were not loading.

### 6.2 Solution

A custom privileged protocol `mill-asset://` is registered in the main process:

- `protocol.registerSchemesAsPrivileged` with `bypassCSP`, `supportFetchAPI`, `stream`.
- `protocol.handle("mill-asset", ...)` in `app.whenReady()` decodes the file path and proxies through `net.fetch(pathToFileURL(...))`.

Service logos are converted to `mill-asset://<encoded-absolute-path>` in `toRendererDefinition`.

The app header logo is served through a `getAppLogoUrl` IPC that resolves `resources/services/assets/mill-logo.png` via the same protocol.

## 7) UI behavior

### 7.1 Header

Header provides:

- Start All / Stop All
- Refresh
- Workspace dropdown menu (choose, reload, import from URL, recent list)
- Settings button
- Theme toggle (light/dark)
- Workspace directory and slug display

### 7.2 Settings view isolation

When the user is in the **workspace settings** view:

- All workspace operations are disabled (Start All, Stop All, Refresh, Workspace menu, Settings button).
- Only the Back button and settings form fields remain active.
- This prevents workspace switches or service operations while editing settings.

### 7.3 Service cards

Each service card displays:

- Service logo (via `mill-asset://` protocol)
- Name and description
- **Service type icon and label** — per-type icon from `react-icons/hi2`:
  - `command` → terminal/command-line icon
  - `node` → code bracket icon
  - `javaBoot` → circle stack icon
  - `docker` → cube icon
  - `docker-compose` → grid/squares icon
- Status lamp
- PID badge, health badge, **prerequisite badge** (green "Prereq ok" / orange "Prereq missing")
- Links section
- Start/Stop buttons (Start disabled when prerequisites fail)
- Log and delete action icons
- Error text or prerequisite warning text at card bottom

### 7.4 Workspace settings view

Rendered as a full in-app view (not modal) with:

- **General** fieldset: Workspace ID (read-only), Workspace Slug
- **AI** fieldset: Provider, API Key (password), Chat Model, Embedding Model
- Help link to `https://docs.qpointz.io/latest/launcher/settings`
- Save action
- Back button to service dashboard

## 8) Fake services for local testing

`resources/services/bin/fake-service.js`:

- emits infinite `ping` logs
- supports optional local HTTP endpoint for health check
- handles `SIGINT`/`SIGTERM` for graceful stop tests

Manifest currently points to fake services for easy smoke testing.

## 9) Build commands

### 9.1 From `apps/mill-desktop`

- dev: `npm run dev`
- build: `npm run build`
- package generic: `npm run electron:build`
- windows x64: `npm run build:win-x64`
- mac x64: `npm run build:mac-x64`
- mac arm64: `npm run build:mac-arm64`
- linux x64: `npm run build:linux-x64`
- linux arm64: `npm run build:linux-arm64`

Note: one target per build; prepare matching service binaries and manifest before packaging.

### 9.2 From `apps/` via Makefile

- `make desktop-install` — install npm dependencies
- `make desktop-build` — build mill-desktop
- `make desktop-launch` — launch mill-desktop in dev mode
- `make desktop-clean` — remove desktop build artifacts

Desktop targets were moved from `ui/Makefile` to `apps/Makefile` as the app lives under `apps/mill-desktop`.

## 10) Known dev/runtime gotchas encountered

### 10.1 IPC bridge unavailable

Observed when Electron preload path or dev URL wiring was mismatched. Mitigations applied:

- robust preload path handling
- guardrails in renderer for missing bridge
- ensure clean dev startup on single Vite port

### 10.2 Vite port mismatch

If `5173` is occupied, Vite can move to `5174` while Electron still targets `5173`, causing confusing behavior.

Recommendation:

- keep `5173` free before `npm run dev`
- restart cleanly if Vite auto-switches ports

### 10.3 Windows cache lock warnings

Transient Electron disk cache warnings were observed on some runs; usually non-fatal for development.

### 10.4 Logos not loading (file:// blocked by sandbox)

Electron's sandbox blocks `file://` protocol in the renderer. Fixed by registering a custom `mill-asset://` protocol. See section 6.

### 10.5 Windows code signing during packaging

`electron-builder` may fail with symlink errors from `winCodeSign`. Workaround: set `CSC_IDENTITY_AUTO_DISCOVERY=false` to build unsigned installers for testing.

### 10.6 window.prompt / window.confirm broken in sandbox

`window.prompt()` and `window.confirm()` do not work in Electron's sandboxed renderer — `prompt` silently returns `null`, breaking the import-from-URL flow.

Fixed by adding two IPC-based replacements:

- `showConfirm(message, detail?)` — delegates to `dialog.showMessageBoxSync` in the main process with OK/Cancel buttons.
- `showPrompt(title, label)` — opens a small modal `BrowserWindow` with a styled input field. Returns trimmed text on OK, `null` on cancel or empty.

Both are exposed via preload as `getApi().showConfirm(...)` and `getApi().showPrompt(...)`. All renderer-side `window.prompt` and `window.confirm` calls were replaced.

## 11) Next candidate improvements

- validate manifest shape at startup with explicit user-facing diagnostics
- add restart policy (`never`, `on-failure`, `always`)
- add health-check strategy per service type (beyond process alive)
- expose workspace settings schema versioning/migration
- replace dummy service-type icons with proper branded SVGs
- add import validation banner ("Imported N docker services, skipped M non-docker services")
- add tooltip on disabled Start button showing exact prerequisite failure message

## 12) CI/CD note (deferred by scope)

Current state prioritizes local bootstrap/package commands. CI matrix/publish jobs are intentionally deferred.

If needed later, define per-target jobs (win x64, mac x64/arm64, linux x64/arm64) and package only target-specific service resources per job.

## 13) Session changelog (this iteration)

Changes made in this session on top of the previous baseline:

1. **Docker & Docker Compose dedicated handlers** — new `DockerServiceLifecycleHandler` and `DockerComposeServiceLifecycleHandler` replacing generic `CommandServiceLifecycleHandler` mapping; each with type-specific prerequisite checks and spawn logic.
2. **Prerequisite check contract** — added `check()` method to `ServiceLifecycleHandler` interface; implemented in all five handler types; wired into `ServiceManager` to gate service starts and surface status in UI.
3. **Service-type icons in cards** — each service card now shows a per-type icon and label (command, node, javaBoot, docker, docker-compose) plus a prerequisite status badge.
4. **Settings view disables workspace operations** — when editing workspace settings, all header workspace/service actions are disabled to prevent conflicting operations.
5. **Custom `mill-asset://` protocol** — replaced broken `file://` logo URLs with a privileged custom protocol registered in the main process; fixes logo display in sandboxed renderer.
6. **Bundled app logo** — copied `mill-orange-2-no-gradients.png` into `resources/services/assets/mill-logo.png`; header loads it via `getAppLogoUrl` IPC.
7. **Desktop Makefile targets moved** — moved `desktop-install`, `desktop-build`, `desktop-launch`, `desktop-clean` from `ui/Makefile` to `apps/Makefile`.
8. **Native confirm/prompt dialogs** — replaced broken `window.prompt`/`window.confirm` with IPC-based `showConfirm` and `showPrompt` using Electron's native dialog and a modal input window; fixes import-from-URL and all confirmation flows.
