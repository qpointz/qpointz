# WI-329 — HTTP MCP Transport (mill-service backend)

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: **A-56**  
Depends on: [WI-327](WI-327-mill-ai-mcp-core.md)

## Problem Statement

Remote agents and the Mill data plane need a single **authoritative MCP backend** co-located with
mill-service. stdio suits local IDE clients only; production and Skymill-backed demos require HTTP
MCP with the same authentication model as other **`/services/**`** providers (`/services/jet`, `/services/export`).

## Goal

Create **`ai/mill-ai-mcp-transport-http`** and wire optional MCP auto-configuration so **mill-service**
(AI edition) exposes capabilities over **Streamable HTTP** when enabled.

v3 **agent runtime** remains LangChain4j ([`v3-foundation-decisions.md`](../../../design/agentic/v3-foundation-decisions.md) §2.3).
MCP is a **protocol transport layer** only. HTTP wire handling uses the **MCP Java SDK**
(`io.modelcontextprotocol.sdk:mcp`) — servlet-based Streamable HTTP
(`HttpServletStreamableServerTransportProvider`) registered in Spring Boot. Capability execution
uses `CapabilityMcpCatalog` + catalog-scoped `CapabilityMcpExecutor` from `mill-ai-mcp-core`.

## Dependency policy (locked)

| Allowed | Forbidden on story MCP modules |
|---------|-------------------------------|
| `io.modelcontextprotocol.sdk:mcp` (via `mcp-bom`) | Third-party AI framework libraries |
| `mill-ai-mcp-core`, `mill-ai-autoconfigure` | AI-framework MCP server starters |
| Spring Boot (servlet registration, `@SpringBootTest`, security filters) | |

Gradle `test` or `testIT` should assert: **HTTP** transport resolves MCP Java SDK + Mill modules;
**stdio bridge** resolves **MCP Java SDK only** (no `mill-ai-mcp-core`).

## In Scope

1. **Gradle module** `ai/mill-ai-mcp-transport-http`:
   - Depends on `mill-ai-mcp-core`, `mill-ai-autoconfigure`, `io.modelcontextprotocol.sdk:mcp` (BOM).
   - Spring Boot for servlet registration + security integration only.
2. **`HttpMcpServerAdapter`** (or equivalent):
   - Builds `McpServer` from MCP Java SDK with `HttpServletStreamableServerTransportProvider`.
   - Registers servlet at configured path (Mill default: `/services/mcp`).
   - Registers tools/prompts/resources from `CapabilityMcpCatalog`; delegates calls to
     catalog-scoped `CapabilityMcpExecutor`.
3. **Configuration properties** (Java, with metadata processor):
   - `mill.ai.mcp.enabled` (default `false`)
   - `mill.ai.mcp.profile` (default `hello-world`)
   - `mill.ai.mcp.capabilities` (default empty list)
   - `mill.ai.mcp.http.endpoint` (default `/services/mcp`) — servlet mount path
4. **Security (POC requirement)**:
   - MCP servlet path under `/services/**` so [`ServicesSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ServicesSecurityConfiguration.java)
     applies when `mill.security.enable=true` (same Bearer/Basic as `/services/jet`, `/services/export`).
   - `testIT`: unauthenticated → 401; authenticated → list/call success.
5. Reuse **`SpringCapabilityDependencyAssembler`** for data-plane profiles (`skymill-ai`, etc.).
6. **Classpath wiring:** add `mill-ai-mcp-transport-http` to `mill-ai-autoconfigure` (or equivalent
   `ai-chat-service` feature path) so `apps:mill-service` bootRun with `mill.ai.mcp.enabled=true`
   registers the servlet without ad-hoc module deps in the example README only.
7. **Embedded test fixture** reused by WI-328 stdio bridge `testIT`.

## Out of Scope

- LangChain4j MCP **server** (LangChain4j MCP is **client**-side only)
- Default enablement in production mill-service config
- Full RBAC (admission stub)
- Replacing REST/SSE chat API
- External OAuth-only MCP security modules (Mill `/services/**` auth for POC)

## Acceptance Criteria

- MCP enabled via `mill.ai.mcp.enabled=true` in test context.
- Streamable HTTP at `/services/mcp` (configurable).
- Security matches `/services/**` when `mill.security.enable=true`.
- Same catalog-scoped executor path as design §7.
- `mill.ai.mcp.*` in configuration metadata.
- Module classpath: MCP Java SDK + Mill modules only for protocol handling.

## Deliverables

- `ai/mill-ai-mcp-transport-http/` module.
- Auto-configuration in `mill-ai-autoconfigure` or co-located per layering review.
- Security + smoke `testIT`; shared HTTP fixture for WI-328.

## Verify (manual)

```bash
./gradlew :apps:mill-service:bootRun \
  --args='--spring.profiles.active=skymill-ai --mill.ai.mcp.enabled=true'
```
