# WI-328 — stdio Local Bridge (Remote HTTP MCP)

Status: `backlog`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: **A-96** (descoped from [A-56](../BACKLOG.md) / [`ai-v3-mcp-server-poc`](../completed/20260622-ai-v3-mcp-server-poc/STORY.md))  
Depends on: [WI-329](../completed/20260622-ai-v3-mcp-server-poc/WI-329-mill-ai-mcp-transport-http.md) (shipped)

> **Descoped** from the MCP POC story (June 2026). Cursor and other clients can use **Streamable HTTP**
> directly (`url` in `mcp.json` → `http://localhost:8080/services/mcp`). A stdio bridge remains
> useful for legacy IDE clients and subprocess-based MCP hosts — revisit when requirements are clear.

## Problem Statement

Local MCP clients (Cursor, IDE agents, LangChain Python via `langchain-mcp-adapters`) historically
spoke **stdio**, but Mill capabilities and the data plane live on **mill-service**. A standalone
stdio server that embeds the full capability registry and data dependencies duplicates deployment
concerns and bypasses Mill security.

## Goal

Create **`ai/mill-ai-mcp-transport-stdio`** as a **thin local MCP bridge**:

- **stdio** toward the local agent (MCP Java SDK server transport)
- **HTTP (Streamable HTTP)** toward the remote Mill MCP backend (WI-329)
- Forwards MCP requests (`tools/list`, `tools/call`, prompts, resources) and **propagates auth**
  credentials to the remote server

The bridge does **not** execute capabilities locally; the authoritative backend is always the HTTP
MCP endpoint on mill-service (or an embedded HTTP MCP test fixture from WI-329).

## Open questions (before implementation)

- Which clients still **require** stdio vs Streamable HTTP (`url` in `mcp.json`)?
- Subprocess model: Gradle `run` vs packaged JAR vs `npx`-style wrapper for Cursor on Windows?
- Credential forwarding: env vars vs `mcp.json` `headers` on the bridge process only?
- Relationship to Python `langchain-mcp-adapters` (direct HTTP already works for WI-330).

## In Scope

1. **Gradle module** `ai/mill-ai-mcp-transport-stdio`:
   - Depends on **MCP Java SDK only** (stdio server transport + HTTP client) — **no** `mill-ai`,
     **no** `mill-ai-mcp-core`. Pure protocol bridge; authoritative catalog/executor live on HTTP backend.
   - **No** `SpringCapabilityDependencyAssembler`, **no** local `CapabilityMcpExecutor` wiring.
   - `application` plugin with main class `io.qpointz.mill.ai.mcp.stdio.McpStdioBridgeKt`.
   - `test` dependency assertion: resolved compile classpath contains only MCP Java SDK (+ Kotlin stdlib/test libs).
2. **`StdioMcpBridgeAdapter`**:
   - stdio MCP server surface for the local client.
   - HTTP MCP client to remote backend (CLI `--remote-url` or env `MILL_MCP_URL`).
   - Forwards `Authorization` (Bearer) and/or HTTP Basic from bridge config/env to remote.
3. **CLI options**:
   - `--remote-url` (required in production) — e.g. `http://localhost:8080/services/mcp`
   - `--token` / env `MILL_MCP_TOKEN` (optional Bearer)
   - `--user` / `--password` (optional Basic; same auth providers as `/services/**` when applicable)
   - Profile/allowlist are configured on the **remote** server (`mill.ai.mcp.*`), not re-filtered
     locally (bridge is transparent).
4. **`testIT` smoke** (`StdioMcpBridgeIT`):
   - Start embedded HTTP MCP server from WI-329 test fixture (in-process, random port).
   - Launch stdio bridge against that URL (in-process stdio client, no subprocess in CI).
   - Assert `tools/list` includes `demo.say_hello` and `tools/call` returns greeting.
   - Assert profile-blocked invoke on remote is visible through bridge (remote rejects).

## Out of Scope

- Local embedded capability execution (no Skymill/data-plane bundling in this module).
- HTTP MCP server implementation (WI-329 — done).

## Acceptance Criteria

- `./gradlew :ai:mill-ai-mcp-transport-stdio:run --args="--remote-url=..."` starts without error
  when remote MCP is reachable.
- `./gradlew :ai:mill-ai-mcp-transport-stdio:testIT` passes bridge smoke against embedded HTTP MCP.
- Cursor MCP config documented: stdio bridge command + env for remote URL and token (alongside
  Streamable HTTP `url` option in design doc §9).
- Unauthenticated calls to a secured remote backend fail (bridge forwards creds; remote enforces).

## Deliverables

- `ai/mill-ai-mcp-transport-stdio/` module (bridge only).
- Bridge configuration properties / CLI docs in design doc §9 (when implemented).
- `testIT` suite per `mill-ai-persistence` pattern.

## Verify (manual)

```bash
# Terminal A — mill-service with MCP enabled (see WI-329 / WI-330)
./gradlew :apps:mill-service:bootRun \
  --args='--spring.profiles.active=skymill-ai-mcp'

# Terminal B — local agent via bridge (or IDE spawns bridge subprocess)
./gradlew :ai:mill-ai-mcp-transport-stdio:run \
  --args='--remote-url=http://localhost:8080/services/mcp' \
  --console=plain
```

## Design reference

- [`docs/design/agentic/v3-mcp-capability-exposure.md`](../../design/agentic/v3-mcp-capability-exposure.md) — §9 (stdio deferred), transport architecture
