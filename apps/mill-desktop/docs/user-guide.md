# Mill Desktop -- User Guide

Mill Desktop is a desktop application for launching, monitoring, and managing local services from a single control panel. It runs on Windows, macOS, and Linux.

---

## Table of Contents

1. [Installation](#installation)
2. [First Launch](#first-launch)
3. [Main Screen](#main-screen)
   - [Header Bar](#header-bar)
   - [Active Services](#active-services)
   - [Available Services](#available-services)
4. [Starting and Stopping Services](#starting-and-stopping-services)
   - [Starting a Single Service](#starting-a-single-service)
   - [Stopping a Single Service](#stopping-a-single-service)
   - [Start All / Stop All](#start-all--stop-all)
5. [Service Cards](#service-cards)
   - [Status Indicator](#status-indicator)
   - [Badges](#badges)
   - [Links](#links)
   - [Logs](#logs)
   - [Deleting a Service](#deleting-a-service)
6. [Workspaces](#workspaces)
   - [What is a Workspace](#what-is-a-workspace)
   - [Choosing a Workspace](#choosing-a-workspace)
   - [Reloading a Workspace](#reloading-a-workspace)
   - [Importing Services from a URL](#importing-services-from-a-url)
   - [Recent Workspaces](#recent-workspaces)
7. [Workspace Settings](#workspace-settings)
   - [Editing Settings](#editing-settings)
   - [Using Settings in Services](#using-settings-in-services)
8. [Service Configuration](#service-configuration)
   - [The Service Manifest File](#the-service-manifest-file)
   - [Service Types](#service-types)
   - [Adding a New Service](#adding-a-new-service)
   - [Configuring a Command Service](#configuring-a-command-service)
   - [Configuring a Node.js Service](#configuring-a-nodejs-service)
   - [Configuring a Java Service](#configuring-a-java-service)
   - [Configuring a Docker Service](#configuring-a-docker-service)
   - [Configuring a Docker Compose Service](#configuring-a-docker-compose-service)
   - [Adding Links](#adding-links)
   - [Shutdown Behavior](#shutdown-behavior)
9. [Prerequisites](#prerequisites)
10. [Dark Mode](#dark-mode)
11. [Exiting the Application](#exiting-the-application)
12. [Troubleshooting](#troubleshooting)
13. [File Locations](#file-locations)

---

## Installation

Download the installer for your platform:

| Platform | File |
|---|---|
| Windows (x64) | `mill-desktop-Setup.exe` |
| macOS (Intel) | `Mill Launcher.dmg` |
| macOS (Apple Silicon) | `Mill Launcher.dmg` |
| Linux (x64) | `Mill Launcher.AppImage` |
| Linux (ARM64) | `Mill Launcher.AppImage` |

Run the installer and follow the on-screen instructions. On Windows, the executable is called `mill-desktop.exe`. On macOS and Linux, the application appears as "Mill Launcher" in your application menu.

## First Launch

When you open Mill Desktop for the first time:

1. A default workspace is created automatically in your user data directory.
2. The bundled sample service definitions and assets are copied into the workspace.
3. The main screen appears showing your available services.

No manual setup is required. You can start using the sample services immediately or configure your own.

## Main Screen

The main screen is divided into two areas: the **header bar** at the top and the **service area** below it.

### Header Bar

The header bar is always visible and provides global controls:

| Element | Description |
|---|---|
| **Mill Launcher** (logo + title) | Application branding. Shows the current workspace path and slug underneath. |
| **N/M running** | Badge showing how many services are currently active out of the total. |
| **Start All** | Starts every service in the workspace, one by one. |
| **Stop All** | Stops all running services at the same time. |
| **Refresh** (circular arrow icon) | Manually refreshes service statuses. The app also auto-refreshes every 2 seconds. |
| **Workspace** | Opens the workspace menu (see [Workspaces](#workspaces)). |
| **Settings** | Opens the workspace settings editor (see [Workspace Settings](#workspace-settings)). |
| **Theme toggle** (sun/moon icon) | Switches between light and dark mode. |

When you are in the Settings view, the Start All, Stop All, Refresh, Workspace, and Settings buttons are disabled to prevent accidental changes while editing.

### Active Services

Services that are currently **running**, **starting**, or **stopping** are displayed as full cards in a responsive grid at the top of the service area. The section is labeled "Active (N)" where N is the count.

Each card shows detailed information about the service including its status, PID, links, and action buttons. See [Service Cards](#service-cards) for details.

### Available Services

Services that are **stopped** or in an **error** state appear as a compact list below the active section, labeled "Available (N)". Each row shows:

- The service logo, name, type icon, and a single-line description
- A **Start** button to launch the service
- A **Delete** button (trash icon) to remove the service from the workspace
- An **Error** badge with tooltip (if the service previously failed)
- A **Log** icon (if the service is in error state, to inspect what went wrong)
- A **Prereq missing** badge (if required software is not installed)

When you start a service from the available list, it moves up to the active section as a full card. When you stop a running service, it returns to the available list.

A horizontal divider separates the two sections when both contain services.

## Starting and Stopping Services

### Starting a Single Service

1. Find the service in the **Available** list at the bottom of the screen.
2. Click the **Start** button on its row.
3. The service moves to the **Active** section and shows a yellow "starting" status lamp.
4. Once the process is running, the lamp turns green and shows "running".

If the service fails to start (missing prerequisite, command not found, immediate crash), it stays in the available list with a red "Error" badge. Hover over the badge to see the error message, or click the log icon to open the full log file.

### Stopping a Single Service

1. Find the running service in the **Active** cards section.
2. Click the **Stop** button on its card.
3. The status lamp turns yellow ("stopping") while the application sends the shutdown signal.
4. Once stopped, the service moves back to the **Available** list.

The application sends a configurable signal (default: `SIGTERM`) and waits for the process to exit. If the process does not exit within the timeout (default: 6 seconds), it is forcefully terminated.

### Start All / Stop All

- **Start All**: Click the button in the header bar. Services are started one at a time in the order they appear in the manifest. If a service fails its prerequisite check, it is skipped and the next service is started.
- **Stop All**: Click the button in the header bar. All running services receive their shutdown signal simultaneously and are stopped in parallel.

## Service Cards

When a service is active (running, starting, or stopping), it is displayed as a full card with the following elements:

### Status Indicator

A colored circle (lamp) in the top-right corner of the card:

| Color | Meaning |
|---|---|
| Green | Service is running normally. |
| Yellow | Service is starting or stopping. |
| Red | Service encountered an error. |
| Gray | Service is stopped. |

### Badges

Below the service name and description, three badges are shown:

- **PID**: Displays the operating system process ID of the running service, or "-" if not running.
- **Healthy / Error**: Shows "Healthy" (gray) if there is no error, or "Error" (red) if the last run produced an error.
- **Prereq ok / Prereq missing**: Shows whether the required runtime (Node.js, Java, Docker, etc.) was found on the system.

### Links

Each service can have a list of clickable links (e.g., "Web UI", "API Docs", "Health Check"). Clicking a link opens it in your system's default web browser. If no links are configured, "No links configured" is displayed.

### Logs

Click the **document icon** on a service card to open the service's log file in your system's default text editor. The log file captures both stdout and stderr from the service process, prefixed with `[stdout]` or `[stderr]`.

Log files are stored in the application's user data directory under `logs/` and are named `<service-id>.log`. Logs are appended across restarts -- they are not cleared when a service starts again.

### Deleting a Service

Click the **trash icon** on a service card (or in the available list row). A confirmation dialog appears:

- If the service is running, the dialog warns that it will be stopped first.
- If you confirm, the service is stopped (if running) and removed from the workspace manifest file.

Deletion only removes the entry from `services.manifest.yaml`. It does not delete any files on disk (logos, scripts, binaries).

## Workspaces

### What is a Workspace

A workspace is a folder on your computer that contains everything Mill Desktop needs to manage a set of services:

- **Service definitions** -- a YAML file listing all services, their commands, arguments, and links.
- **Service assets** -- logos, scripts, binaries, configuration files used by services.
- **Workspace settings** -- identity and configuration values for this workspace.

All workspace data lives inside a `.mill` subdirectory within the workspace folder:

```
my-workspace/
  .mill/
    settings.json
    services.manifest.yaml
    services/
      assets/
      bin/
```

You can have multiple workspaces on your computer (e.g., one per project or environment) and switch between them.

### Choosing a Workspace

1. Click **Workspace** in the header bar.
2. Select **Choose workspace**.
3. A folder picker dialog opens. Navigate to the directory you want to use as a workspace and select it.
4. All running services are stopped.
5. If the chosen directory does not have a `.mill` folder yet, one is created with default settings and the bundled sample services.
6. The service list reloads from the new workspace.

### Reloading a Workspace

If you have edited `services.manifest.yaml` by hand (in a text editor), you need to reload the workspace for changes to take effect:

1. Click **Workspace** in the header bar.
2. Select **Reload workspace**.
3. A confirmation dialog appears warning that all running services will be stopped.
4. Confirm to stop services and reload the manifest from disk.

### Importing Services from a URL

You can import service definitions from a remote JSON file hosted on the internet:

1. Click **Workspace** in the header bar.
2. Select **Import services from URL**.
3. A prompt appears asking for a URL. Paste the full URL to a service definitions JSON file and click OK.
4. The application downloads the JSON, filters it to only include services of type `docker` or `docker-compose`, and writes them to your workspace manifest.
5. All running services are stopped and the service list is reloaded.

The remote JSON must follow the same format as the service manifest (a `{ "services": [...] }` object). Only Docker and Docker Compose services are imported; other types are ignored.

**Important**: Importing replaces the entire workspace manifest. If you want to keep existing services, back up your `services.manifest.yaml` first.

### Recent Workspaces

The application remembers up to 12 workspaces you have used previously:

1. Click **Workspace** in the header bar.
2. Under **Recent workspaces**, click any entry to switch to that workspace.
3. All running services are stopped and the new workspace is loaded.

Recent workspaces that no longer exist on disk are automatically removed from the list.

## Workspace Settings

### Editing Settings

1. Click **Settings** in the header bar.
2. The service view is replaced by the settings editor, organized into groups.
3. Edit the fields:

   **General:**
   - **Workspace ID** -- a unique alphanumeric identifier for this workspace (read-only).
   - **Workspace Slug** -- a short human-readable name (alphanumeric, no spaces).

   **AI:**
   - **Provider** -- AI provider name (e.g., `openai`).
   - **API Key** -- API key for the AI provider (displayed as a password field).
   - **Chat Model** -- the model to use for chat (e.g., `gpt-4o`).
   - **Embedding Model** -- the model to use for embeddings (e.g., `text-embedding-3-small`).

4. Click **Save Workspace Settings** to persist changes to `<workspace>/.mill/settings.json`.
5. Click **Back** to return to the service view.
6. Use the **Help** link next to the title to open the settings documentation at `https://docs.qpointz.io/latest/launcher/settings`.

While in the settings view, all service management controls in the header are disabled.

### Using Settings in Services

Service configurations support placeholder syntax that is resolved when a service starts. Two namespaces are available: workspace (`ws`) and service (`svc`).

#### Workspace placeholders -- `[[ws:<key>]]`

Reference workspace-level settings. For example, if your workspace slug is `staging`, then the argument `--profile=[[ws:general.workspaceSlug]]` becomes `--profile=staging` when the service starts.

| Placeholder | Value |
|---|---|
| `[[ws:dir]]` | Full path to the workspace directory. |
| `[[ws:settingsPath]]` | Full path to the workspace's `settings.json` file. |
| `[[ws:general.workspaceId]]` | The Workspace ID setting. |
| `[[ws:general.workspaceSlug]]` | The Workspace Slug setting. |
| `[[ws:ai.provider]]` | AI provider. |
| `[[ws:ai.apiKey]]` | AI API key. |
| `[[ws:ai.chatModel]]` | AI chat model. |
| `[[ws:ai.embeddingModel]]` | AI embedding model. |
| `[[ws:workspaceId]]` | Alias for `general.workspaceId`. |
| `[[ws:workspaceSlug]]` | Alias for `general.workspaceSlug`. |

#### Service placeholders -- `[[svc:<key>]]`

Reference properties of the service being started. Useful when you want the service to know its own identity or have an isolated working area.

| Placeholder | Value |
|---|---|
| `[[svc:id]]` | The service ID from the manifest. |
| `[[svc:name]]` | The service display name. |
| `[[svc:description]]` | The service description. |
| `[[svc:type]]` | The service type (`command`, `node`, `javaBoot`, `docker`, `docker-compose`). |
| `[[svc:logPath]]` | Absolute path to the service's log file. |
| `[[svc:dir]]` | Service-specific directory at `<workspace>/services/<service-id>/`. |

#### Example combining both

```yaml
args:
  - "--tenant=[[ws:general.workspaceSlug]]"
  - "--service-name=[[svc:name]]"
  - "--data-dir=[[svc:dir]]/data"
env:
  APP_LOG: "[[svc:logPath]]"
  APP_WORKSPACE: "[[ws:dir]]"
  AI_KEY: "[[ws:ai.apiKey]]"
```

#### Where placeholders work

Placeholders are resolved in these fields of the spawned process: `command`, `args`, `cwd`, and `env` values.

If a placeholder references a key that does not exist, it is left unchanged (not replaced with an empty string), making misconfigurations easy to spot in log files.

## Service Configuration

### The Service Manifest File

All services are defined in a single YAML file located at:

```
<workspace>/.mill/services.manifest.yaml
```

The file contains a `services` list. Each element describes one service. After editing this file, use **Workspace > Reload workspace** in the app to apply changes.

### Service Types

Each service has a type that determines how it is started and what prerequisites are checked:

| Type | Description | Requires |
|---|---|---|
| `command` | Runs any executable or script. | The command must exist on PATH or at the specified path. |
| `node` | Runs a Node.js script. | Node.js must be installed. |
| `javaBoot` | Runs a Java Spring Boot JAR file. | Java must be installed. |
| `docker` | Runs a Docker command. | Docker CLI must be installed. |
| `docker-compose` | Runs Docker Compose. | Docker Compose (v1 or v2) must be installed. |

If `serviceType` is not specified, it defaults to `command`.

### Adding a New Service

Open `<workspace>/.mill/services.manifest.yaml` in a text editor and add a new entry to the `services` list. Here is a minimal example:

```yaml
services:
  - id: my-service
    name: My Service
    description: A short description of what this service does
    logoPath: services/assets/my-logo.png
    serviceType: command
    lifecycle:
      start:
        command: echo
        args:
          - Hello from Mill Desktop
    links: []
```

The `serviceType` field is required and determines which handler interprets the `lifecycle.start` config. All lifecycle phases (`start`, `stop`, `clean`) use the same flat key-value structure -- the handler knows which keys to expect.

You can also add an optional `readme` field with an HTTP URL to documentation:

```yaml
  - id: my-service
    name: My Service
    readme: https://docs.example.com/my-service
    # ... other fields ...
```

When present, a "Readme" link appears in both the active service card and the available service list.

Place any logo images in `<workspace>/.mill/services/assets/`. The `logoPath` is relative to the `.mill` directory.

After adding the entry, reload the workspace in the app.

### Configuring a Command Service

A command service runs any executable:

```yaml
- id: my-tool
  name: My Tool
  description: Custom CLI tool
  logoPath: services/assets/tool.png
  serviceType: command
  lifecycle:
    start:
      command: my-tool
      args:
        - serve
        - --port
        - "4000"
      cwd: "."
      env:
        CONFIG_PATH: "[[ws:dir]]/config.json"
      shell: false
  links:
    - label: Dashboard
      url: http://localhost:4000
```

- `command`: The executable name or path. If it is just a name (like `my-tool`), it must be on your system PATH.
- `args`: Arguments passed to the command.
- `cwd`: Working directory. Use `"."` for the workspace directory, or provide an absolute or relative path.
- `env`: Extra environment variables. Use `[[ws:...]]` placeholders for dynamic values.
- `shell`: Set to `true` to run via the system shell (required for shell built-ins like `echo` on some systems). Defaults to `true` on Windows.

### Configuring a Node.js Service

```yaml
- id: api-server
  name: API Server
  description: Express API on port 3000
  logoPath: services/assets/api.svg
  serviceType: node
  lifecycle:
    start:
      scriptPath: services/bin/server.js
      nodeArgs:
        - --max-old-space-size=512
      scriptArgs:
        - --port
        - "3000"
      env:
        NODE_ENV: production
  links:
    - label: API
      url: http://localhost:3000
```

- `scriptPath` (required): Path to your `.js` file, relative to the workspace directory.
- `nodeArgs`: Arguments for the `node` binary itself (memory limits, flags, etc.).
- `scriptArgs`: Arguments for your script.

### Configuring a Java Service

```yaml
- id: backend
  name: Backend Service
  description: Spring Boot application
  logoPath: services/assets/spring.svg
  serviceType: javaBoot
  lifecycle:
    start:
      jarPath: services/bin/backend.jar
      jvmArgs:
        - -Xmx1g
        - "-Dspring.profiles.active=[[ws:general.workspaceSlug]]"
      appArgs:
        - --server.port=8080
  links:
    - label: Health
      url: http://localhost:8080/actuator/health
```

- `jarPath` (required): Path to the `.jar` file, relative to the workspace directory.
- `jvmArgs`: JVM arguments (memory, system properties, etc.). Placed before `-jar`.
- `appArgs`: Application arguments. Placed after the JAR path.

### Configuring a Docker Service

```yaml
- id: redis
  name: Redis
  description: Redis 7 container
  logoPath: services/assets/redis.svg
  serviceType: docker
  lifecycle:
    start:
      command: docker
      args:
        - run
        - --rm
        - --name
        - mill-redis
        - -p
        - "6379:6379"
        - redis:7-alpine
    stop:
      command: docker
      args:
        - stop
        - mill-redis
    clean:
      command: docker
      args:
        - rm
        - -f
        - mill-redis
  links: []
  gracefulShutdown:
    timeoutMs: 15000
```

All three phases (`start`, `stop`, `clean`) use the same flat structure: `command` + `args`.

**Tip**: Use `--rm` so the container is automatically removed when stopped. Use `--name` to give the container a predictable name.

**Important**: Docker containers do not respond to process signals the way native processes do. You should add a `lifecycle.stop` block so that Mill Desktop runs the proper `docker stop` command. The optional `lifecycle.clean` block runs when the service is deleted or manually cleaned (e.g., `docker rm -f` to remove a leftover container).

### Configuring a Docker Compose Service

```yaml
- id: infra
  name: Infrastructure
  description: Database and cache via Compose
  logoPath: services/assets/compose.svg
  serviceType: docker-compose
  lifecycle:
    start:
      command: docker
      args:
        - compose
        - -f
        - "[[ws:dir]]/docker-compose.yml"
        - up
    stop:
      command: docker
      args:
        - compose
        - -f
        - "[[ws:dir]]/docker-compose.yml"
        - down
    clean:
      command: docker
      args:
        - compose
        - -f
        - "[[ws:dir]]/docker-compose.yml"
        - down
        - --volumes
        - --rmi
        - local
  links: []
  gracefulShutdown:
    timeoutMs: 30000
```

The application auto-detects whether you have Docker Compose v2 (`docker compose`) or v1 (`docker-compose`). If neither is found, the prerequisite check fails.

**Tip**: Set a longer `timeoutMs` for Compose stacks, as they may take longer to shut down cleanly.

### Adding Links

Each service can have clickable links that open in your browser:

```yaml
links:
  - label: Web UI
    url: http://localhost:8080
  - label: API Docs
    url: http://localhost:8080/docs
  - label: Metrics
    url: http://localhost:8080/metrics
```

Links are displayed on the active service card. They are only useful while the service is running.

### Shutdown Behavior

You can control how Mill Desktop stops each service:

```yaml
gracefulShutdown:
  signal: SIGTERM
  timeoutMs: 8000
```

- `signal`: The signal sent to the process. Use `SIGTERM` for most services, `SIGINT` for Docker and interactive tools. Default: `SIGTERM`.
- `timeoutMs`: How long to wait (in milliseconds) for the process to exit after the signal. If it does not exit in time, the process is forcefully killed. Default: `6000` (6 seconds).

If `gracefulShutdown` is not specified, defaults are used.

### Lifecycle Phases

Each service supports three lifecycle phases under the `lifecycle` key: `start`, `stop`, and `clean`. All three phases use the same flat key-value structure. The handler selected by `serviceType` interprets the config.

- **`start`** -- passed to the handler. Keys depend on the service type (see examples above).
- **`stop`** -- optional custom stop command. When present, Mill Desktop runs this instead of sending a signal to the child process. Expects `command`/`args`.
- **`clean`** -- optional cleanup command. Runs when the service is deleted, or when the user clicks the **clean** button (circular arrow icon). Expects `command`/`args`.

```yaml
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

When a `lifecycle.stop` is present:

1. Mill Desktop spawns the stop command instead of signaling the child process.
2. Output is logged with `[stop:stdout]`/`[stop:stderr]` prefixes.
3. After the stop command finishes (or `gracefulShutdown.timeoutMs` expires), the original child process is cleaned up.

When a `lifecycle.clean` is present:

1. It runs automatically before a service is removed from the manifest (via the delete action).
2. It can also be triggered manually via the clean button in the UI.
3. Output is logged with `[clean:stdout]`/`[clean:stderr]` prefixes.

All phases support `[[ws:*]]` and `[[svc:*]]` placeholders in command, args, cwd, and env. If any placeholder cannot be resolved, the process will not start and an error is shown.

All lifecycle events are written to the service log file with timestamps (`[START]`, `[STOP]`, `[ERROR]`, `[CLEAN]`, `[DELETE]`).

### Log Files

Service log files are stored in `<userData>/logs/` and named `{workspaceId}_{serviceId}.log`. This ensures uniqueness across workspaces and machines. Open the log via the log icon in the UI.

## Prerequisites

Before starting a service, Mill Desktop checks that the required runtime is installed on your system:

- **Command services**: Checks that the specified command exists on PATH or at the given file path.
- **Node.js services**: Checks that `node` is available.
- **Java services**: Checks that `java` is available.
- **Docker services**: Checks that `docker` is available.
- **Docker Compose services**: Checks for `docker compose` (v2) first, then `docker-compose` (v1).

If a prerequisite is missing:

- The service shows a **Prereq missing** badge in the available list.
- The **Start** button is disabled for that service.
- A message explaining what is missing is shown (e.g., "Node.js runtime is not installed or not in PATH.").

To fix: install the missing software and restart Mill Desktop (or click Refresh).

## Dark Mode

Click the **sun/moon icon** in the top-right corner of the header to switch between light and dark themes. Your preference is remembered between sessions.

## Exiting the Application

When you close the Mill Desktop window:

- If **no services are running**, the application exits immediately.
- If **services are running**, a confirmation dialog appears: "Services are currently running. All running services will be stopped before exit. Continue?"
  - Click **Stop Services and Exit** to gracefully stop all services and then close the app.
  - Click **Cancel** to keep the app open.

All child processes are always cleaned up before the application exits. No orphaned processes are left behind.

## Troubleshooting

### Service fails to start with "Prereq missing"

The required runtime is not installed or not on your system PATH. Install the software (Node.js, Java, Docker, etc.) and click Refresh.

### Service starts but immediately shows "Error"

Open the service log (click the log icon) to see the error output. Common causes:

- Port already in use by another application
- Missing configuration file referenced in the service arguments
- Incorrect path to a JAR file or script

### "No services configured" message

Your workspace manifest has no service entries. Either:
- Edit `<workspace>/.mill/services.manifest.yaml` and add services manually, or
- Use **Workspace > Import services from URL** to download service definitions.

### Links open but the page does not load

The service may still be starting up. Wait a few seconds for it to become fully operational before clicking links. Check the service log for startup progress.

### Logos appear broken

If service logos show as broken images, verify that:
- The `logoPath` in the service definition points to an existing file.
- The path is relative to the `.mill` directory in your workspace.
- The image file format is SVG, PNG, or JPG.

### Import from URL does nothing

- Check that the URL is accessible and returns valid JSON.
- The JSON must have a `{ "services": [...] }` structure.
- Only services with `serviceType` set to `docker` or `docker-compose` are imported. Other types are filtered out.

### Settings changes are not reflected in running services

Workspace settings substitutions (`[[ws:...]]`) are applied at service start time. If you change a setting, you need to stop and restart the affected services for the new values to take effect.

## File Locations

| Item | Location |
|---|---|
| Workspace manifest | `<workspace>/.mill/services.manifest.yaml` |
| Workspace settings | `<workspace>/.mill/settings.json` |
| Service assets | `<workspace>/.mill/services/` |
| Service logs | `<userData>/logs/<service-id>.log` |
| Workspace selection | `<userData>/workspace.json` |
| Recent workspaces | `~/.mill/recent-workspaces.json` |

The `<userData>` directory is the OS-specific application data folder:

| Platform | Path |
|---|---|
| Windows | `%APPDATA%\mill-desktop\` |
| macOS | `~/Library/Application Support/mill-desktop/` |
| Linux | `~/.config/mill-desktop/` |
