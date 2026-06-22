# AI v3 MCP Server POC — Review Package

**Story:** [`STORY.md`](STORY.md)  
**Branch:** `feat/ai-v3-mcp-server-poc`  
**Backlog:** [A-56](../../BACKLOG.md) · [A-31](../../BACKLOG.md)  
**Design:** [`docs/design/agentic/v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)

---

## Summary

Expose the v3 **AI capability registry** as MCP tools, prompts, and resources. **No bespoke MCP
catalog** — every MCP surface derives from `CapabilityProvider` + YAML manifest in `mill-ai`.

**Transport model:** **HTTP Streamable MCP on mill-service** is authoritative (data + security).
Clients (Cursor 0.48+, LangChain) connect **directly over HTTP**. **stdio bridge** descoped to backlog
**[A-96](../../BACKLOG.md)** / [WI-328](../../backlog/WI-328-mill-ai-mcp-transport-stdio.md).

**Protocol stack:** **MCP Java SDK** (`io.modelcontextprotocol.sdk:mcp`) for HTTP servlet. LangChain4j =
in-process agent runtime only.

**Delivery:** Implement **WI-325–WI-327, WI-329, WI-330** per [`RULES.md`](../../RULES.md) — one commit
per WI, all tests passing, **MR mergeable**. **WI-328** deferred. Story archive/closure only on
explicit user request.

---

## Locked decisions

1. **Core + transport split** — `mill-ai-mcp-core` framework-free; HTTP hosts executor; stdio bridge deferred (A-96)
2. **Tool names** — `{capabilityId}.{toolName}` (e.g. `demo.say_hello`)
3. **Per-capability opt-out** — YAML `mcp.enabled` (default `true`); controls **whole** capability
4. **Server allowlist** — `mill.ai.mcp.capabilities`: empty = all eligible; non-empty = only listed ids
5. **Filter order** — registry → `mcp.enabled` → allowlist → profile → admission; **same set for list and invoke**
6. **Tool kind** — expose `QUERY`/`CAPTURE` on descriptors; **no** kind-based MCP logic in POC
7. **HTTP backend** — mill-service; path **`/services/mcp`** (provider surface alongside jet/export; `/services/**` auth)
8. **MCP Java SDK** — sole protocol dependency; `mcp-bom` in `libs.versions.toml`
9. **Security** — MCP HTTP authenticated like `/services/jet` and `/services/export` when `mill.security.enable=true`; bridge forwards creds
10. **Authoring YAML defaults** — **undecided**; do not change authoring manifests yet
11. **WI order** — WI-327 core → **WI-329 HTTP** → WI-330 (WI-328 stdio bridge → backlog A-96)
12. **Catalog vs execution** — catalog from manifest YAML (no `create()`); invoke via `create(context, deps)`
13. **stdio bridge deps** — MCP Java SDK only; no `mill-ai-mcp-core`

---

## Work items (in scope for MR)

| WI | Title |
|----|-------|
| WI-325 | Design doc (drafted) |
| WI-326 | External asset descriptors + tool `kind` metadata |
| WI-327 | `mill-ai-mcp-core` |
| WI-329 | HTTP MCP backend + Mill security |
| WI-330 | LangChain + mill-service Skymill (manual smoke) |

**Descoped:** WI-328 stdio bridge → backlog [A-96](../../BACKLOG.md).

---

## Review findings (addressed)

| Severity | Finding | Resolution |
|----------|---------|------------|
| High | Profile/allowlist only on catalog list | Catalog-scoped executor (WI-327) |
| Medium | WI-330 two-terminal stdio invalid | Python spawns bridge; mill-service in separate terminal |
| Low | smoke.sh curl on stdio | subprocess JSON-RPC; curl for HTTP MCP only |
| — | Transport architecture revision | HTTP authoritative on mill-service; stdio = bridge |
| — | Protocol stack | MCP Java SDK only; LangChain4j agent runtime unchanged |

---

## Consistency audit (2026-06-22)

### Resolved in this pass

| Issue | Was | Fixed to |
|-------|-----|----------|
| STORY module table auth | `/api/**` auth | `/services/mcp`, `/services/**` auth |
| WI-325 filter order | omitted allowlist step | full five-step order |
| WI-330 / design §10 | "optional WI-330" | in story scope for MR |
| Design §3 mermaid | catalog/executor → stdio | stdio bridge-only edge to HTTP |
| Design §5.2 / REVIEW security | "same auth as REST" | `/services/**` providers |
| Design §7 deps | "Skymill launcher" | mill-service `skymill-ai` + assembler |
| WI-326 | duplicate `3.` numbering; WI-328 = "full server" | renumbered; bridge + HTTP |
| WI-327 | "stdio CLI" for allowlist | mill-service config only |
| WI-329 problem | REST/SSE APIs | `/services/**` providers |
| Bridge remote URL | `mill.ai.mcp.remote.url` vs env | canonical: `--remote-url` / `MILL_MCP_URL` |
| STORY §191 heading | "Optional integration demo" | "Integration demo" |
| STORY test table | `CapabilityMcpSettingsTest` under mcp-core | under `mill-ai` manifest row |
| WI-327 22-tool AC | ambiguous vs hello-world testIT | unit-test-only with full registry, no profile |

### Open items (need decision at implementation)

| Severity | Item | Notes |
|----------|------|-------|
| Low | **Authoring `mcp.enabled: false`** | Story marks **undecided**; design §15 still lists authoring tools in the 23-tool count. No YAML change until decided — document only. |
| Low | **Production default enablement** | Out of scope for runtime defaults; WI-329 explicitly requires autoconfigure classpath wiring so **manual** mill-service demos work with `mill.ai.mcp.enabled=true`. |

### Resolved in review feedback (2026-06-22)

| Severity | Finding | Resolution |
|----------|---------|------------|
| High | Catalog underspecified — tools only on materialized `Capability` | **Locked:** catalog = manifest metadata (`CapabilityManifest.load` + `declaredTools()`); executor = `create(context, deps)` at invoke only. Design §5.3, WI-327. |
| Medium | Tool inventory count wrong (22 vs 23) | Design §15 + WI-327 + STORY → **23 tools, 20 QUERY, 3 CAPTURE** |
| Low | stdio bridge dependency policy contradictory | **Locked:** stdio = **MCP Java SDK only**; no `mill-ai-mcp-core`. STORY, design §2, WI-328 aligned. |
| Medium | WI-328 manual verify missing MCP enable flag | bootRun args include `--mill.ai.mcp.enabled=true` (matches WI-329/WI-330) |
| Low | `declaredTools()` omitted output schema | **Locked:** YAML input+output in `declaredTools()`; MCP wire uses input only; `ExternalCapabilityAssetDescriptor.Tool` carries both |

---

## Deferred (post-MR / undecided)

| Item | Status |
|------|--------|
| `mcp.enabled: false` on authoring capabilities in repo YAML | **Undecided** |
| `mill-service` production MCP enablement by default | Opt-in |
| External OAuth MCP modules | Mill `/services/**` auth first |
| Per-tool / kind-based MCP blocking | Future |
| Story closure (archive, MILESTONE, squash) | Explicit user request |

---

## Verify (before MR)

```bash
./gradlew :ai:mill-ai:test --tests "*CapabilityManifest*"
./gradlew :ai:mill-ai-mcp-core:test
./gradlew :ai:mill-ai-mcp-transport-http:testIT
# WI-330 manual: mill-service + misc/examples/ai-mcp-langchain-skymill/
```

---

## Document index

| Path | Role |
|------|------|
| [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) | Normative design + tool inventory §15 |
| [`v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md) | `mcp:` YAML schema |
