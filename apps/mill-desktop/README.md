# Mill Desktop

Cross-platform Electron desktop application for managing local services. Start, stop, monitor, and configure services such as Java applications, Node.js scripts, Docker containers, and shell commands from a single control panel.

## Quick Start

```bash
cd apps/mill-desktop
npm install
npm run dev
```

## Architecture

```
Renderer (React + Mantine)  <--IPC-->  Main Process (ServiceManager)  --spawn-->  Service Child Processes
```

- **Renderer**: React UI with Mantine components. Shows active service cards and an available-services list. Communicates with the main process via a secure IPC bridge.
- **Main process**: Manages workspaces, service lifecycle, log streaming, and child process orchestration. All services run as child processes and are cleaned up on exit.
- **Shared types**: `shared/service.ts` defines contracts used by both processes.

## Service Types

| Type | Description | Prerequisite |
|---|---|---|
| `command` | Arbitrary executable | Command on PATH or at path |
| `node` | Node.js script | `node` installed |
| `javaBoot` | Spring Boot JAR | `java` installed |
| `docker` | Docker CLI | `docker` installed |
| `docker-compose` | Docker Compose | `docker compose` or `docker-compose` |

## Workspaces

A workspace is a directory containing `.mill/services.manifest.yaml` (service definitions), `.mill/settings.json` (workspace settings), and `.mill/services/` (assets). The app bootstraps a default workspace on first launch. Users can switch, reload, and import services via the UI.

## Settings Substitution

Service args, env, cwd, and command support placeholders that are replaced at start time. Two namespaces are available:

**Workspace placeholders** -- `[[ws:<key>]]`:

| Placeholder | Value |
|---|---|
| `[[ws:dir]]` | Workspace directory path |
| `[[ws:settingsPath]]` | Path to settings.json |
| `[[ws:general.workspaceId]]` | Workspace ID |
| `[[ws:general.workspaceSlug]]` | Workspace slug |
| `[[ws:ai.provider]]` | AI provider |
| `[[ws:ai.apiKey]]` | AI API key |
| `[[ws:ai.chatModel]]` | AI chat model |
| `[[ws:ai.embeddingModel]]` | AI embedding model |
| `[[ws:workspaceId]]` | Alias for `general.workspaceId` |
| `[[ws:workspaceSlug]]` | Alias for `general.workspaceSlug` |

**Service placeholders** -- `[[svc:<key>]]`:

| Placeholder | Value |
|---|---|
| `[[svc:id]]` | Service ID |
| `[[svc:name]]` | Service display name |
| `[[svc:description]]` | Service description |
| `[[svc:type]]` | Service type (command, node, javaBoot, docker, docker-compose) |
| `[[svc:logPath]]` | Absolute path to the service log file |
| `[[svc:dir]]` | Service-specific directory (`<workspace>/services/<id>/`) |

## Lifecycle Phases

Each service has three optional lifecycle phases under `lifecycle`: `start`, `stop`, and `clean`. All three phases use the same flat config shape -- the handler selected by `serviceType` interprets the keys.

- **`start`** -- passed to the handler based on `serviceType`. The `command` handler expects `command`/`args`; the `node` handler expects `scriptPath`/`nodeArgs`/`scriptArgs`; the `javaBoot` handler expects `jarPath`/`jvmArgs`/`appArgs`.
- **`stop`** -- custom stop command (runs instead of process signal). Expects `command`/`args`.
- **`clean`** -- cleanup command (runs on delete or manual clean action). Expects `command`/`args`.

```yaml
# Docker example (all phases consistent)
serviceType: docker
lifecycle:
  start:
    command: docker
    args: [run, --rm, --name, my-svc, my-image]
  stop:
    command: docker
    args: [stop, my-svc]
  clean:
    command: docker
    args: [rm, -f, my-svc]
```

```yaml
# Node example
serviceType: node
lifecycle:
  start:
    scriptPath: server.js
    scriptArgs: [--port, "3000"]
```

All phases support `[[ws:*]]` and `[[svc:*]]` placeholders. If any placeholder cannot be resolved, the process will not start and an error is shown. Output is logged with phase prefixes (`[stop:stdout]`, `[clean:stderr]`).

All lifecycle events (start, stop, error, clean, delete) are written to the service's log file with timestamps.

The clean command runs automatically when a service is deleted, and can also be triggered manually via the clean button (circular arrow icon) in the UI.

## Log Files

Service log files are stored in the application's user data directory under `logs/`. Each file is named `{workspaceId}_{serviceId}.log`, ensuring uniqueness across workspaces and machines.

## Build

| Command | Description |
|---|---|
| `npm run dev` | Development mode (Vite + Electron hot reload) |
| `npm run electron:build` | Package for current platform |
| `npm run build:win-x64` | Windows x64 NSIS installer |
| `npm run build:mac-x64` | macOS Intel DMG |
| `npm run build:mac-arm64` | macOS Apple Silicon DMG |
| `npm run build:linux-x64` | Linux x64 AppImage |
| `npm run build:linux-arm64` | Linux ARM64 AppImage |

Output goes to `release/`. The executable is named `mill-desktop` and the display name is "Mill Launcher".

## Project Structure

```
apps/mill-desktop/
  electron/                     # Main process (TypeScript)
    main.ts                     # ServiceManager, IPC, window, protocol
    preload.ts                  # Secure IPC bridge
    lifecycle/                  # Service type handlers
      ServiceLifecycleHandler.ts
      ServiceLifecycleFactory.ts
      handlers/                 # command, node, javaBoot, docker, docker-compose
  shared/
    service.ts                  # Shared TypeScript interfaces
  src/                          # Renderer (React + Mantine)
    App.tsx                     # Root component, state management
    components/
      layout/                   # AppShell, AppHeader
      service/                  # ServiceCard, StoppedServiceRow, StatusLamp
  resources/                    # Bundled default workspace resources
    services.manifest.yaml
    services/assets/
    services/bin/
  docs/                         # Documentation
  vite.config.ts
  package.json
```

## Documentation

| Document | Description |
|---|---|
| [User Guide](docs/user-guide.md) | End-user documentation: installation, UI walkthrough, service configuration examples, troubleshooting |
| [Cold Start Notes](docs/cold-start-notes.md) | Implementation context, architecture decisions, runtime behavior, known gotchas |
| [TUI Plan](docs/tui-plan.md) | Plan for an interactive terminal UI (Ink/React) sharing core logic with the GUI |
| [Service Enhancements Plan](docs/service-enhancements-plan.md) | Plan for per-service settings, service directories, cloning, and prerequisite auto-installation |
