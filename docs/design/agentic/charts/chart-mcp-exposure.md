# Chart mapping MCP exposure (Gap 20)

**Status:** Locked design input
**Principle:** Chart artifacts are **renderer-independent** semantic specs. MCP must expose the full
tool surface an external agent needs to discover chart types, validate SQL, probe schema, fetch bounded
rows, and produce a validated chart spec — without embedding ECharts or mill-ui.

**Profile:** `data-analysis` (`mill.ai.mcp.profile=data-analysis`)
**Reference:** [`v3-mcp-capability-exposure.md`](../v3-mcp-capability-exposure.md) §15

---

## Locked decision

When **`chart-mapping`** ships, **`data-analysis`** MCP clients get **all tools required** for a
standalone chart workflow. Exposure is **automatic** from capability YAML (`mcp.enabled: true`,
profile filter) — same as existing capabilities. **WI-368** updates the MCP inventory doc and adds
catalog tests mirroring **WI-341**.

External agents **do not** depend on in-process chat artifact persistence or mill-ui compilation.
Tool return payloads must carry **sufficient structured data** to render elsewhere.

---

## Required MCP tool surface

| Stage | Capability | MCP tool | Purpose |
|-------|------------|----------|---------|
| **Ground schema** | `schema` | `list_schemas`, `list_tables`, `list_columns`, … | Resolve tables/columns before SQL |
| **Dialect** | `sql-dialect` | `get_sql_dialect_conventions`, … | SQL conventions |
| **Validate query** | `sql-query` | `validate_sql` | Validate SQL; returns normalized SQL + validation wrapper |
| **Result schema** | `sql-query` | `describe_sql` | Column names/types for encodings (**no rows**) |
| **Result data** | `sql-query` | `execute_sql` | Bounded rows for client-side rendering (**optional** if agent only needs spec) |
| **Chart catalog** | `chart-mapping` | `list_supported_charts` | Supported `chartTypes`, encodings, constraints, defaults |
| **Chart spec** | `chart-mapping` | `validate_chart_spec` | Validate semantic chart config; **`normalizedChart`** on success |

MCP names: `{capabilityId}.{toolName}` (e.g. `chart-mapping.validate_chart_spec`).

### Minimum data for external reuse

After a successful orchestration, an external agent should hold:

| Data | Source |
|------|--------|
| Validated **SQL** | `validate_sql` result (`normalizedSql` / `sql`) |
| **Column schema** | `describe_sql` → `schema[]` (`name`, `type`, `nullable`, optional `nativeType`) |
| **Chart catalog** | `list_supported_charts` → types, roles, options |
| **Semantic chart spec** | `validate_chart_spec` → `normalizedChart` (`sql`, `columns`, `charts[]`, encodings, options) |
| **Row snapshot** (optional) | `execute_sql` → `rows[]` + truncation metadata |

None of these require ECharts JSON. The external client compiles/renders locally (or uses
[`echarts-compiler-contract.md`](./echarts-compiler-contract.md) as a reference implementation in
mill-ui only).

---

## Recommended external agent flow

```text
1. chart-mapping.list_supported_charts     (when chart type / constraints unknown)
2. schema.* + sql-dialect.*                (ground SQL)
3. sql-query.validate_sql                  (validated SQL)
4. sql-query.describe_sql                  (schema for encodings)
5. chart-mapping.validate_chart_spec       (normalizedChart — renderer-independent)
6. sql-query.execute_sql                   (optional — rows for local chart render)
```

Same orchestration rules as in-process **`data-analysis`**: `validate_sql` before
`describe_sql` / `execute_sql`; chart-mapping does not generate or rewrite SQL.

---

## MCP assets beyond tools

| Asset | MCP surface | External use |
|-------|-------------|--------------|
| `generated-chart` artifact schema | Resource `mill://artifacts/generated-chart` | Contract discovery |
| `chart-mapping` descriptor | Resource `mill://capabilities/chart-mapping` | Capability metadata |
| `chart-mapping.generated-chart` protocol | Resource `mill://capabilities/chart-mapping/protocols/...` | Schema only — **not** model-invoked (Gap 19) |
| Chart-mapping prompts | MCP prompts `chart-mapping/chart-mapping.system`, … | Optional for hosted agents |

In-process chat emits `generated-chart` via **`OnToolSuccess`** (Gap 19). MCP **`tools/call`**
returns the **`validate_chart_spec`** tool result including **`normalizedChart`** — sufficient for
external persistence without SSE/chat replay.

---

## Profile matrix

| Profile | `chart-mapping.*` | `sql-query.describe_sql` / `execute_sql` | Typical client |
|---------|-------------------|------------------------------------------|----------------|
| **`data-analysis`** | **Yes** | **Yes** | Chart + SQL external agents |
| `schema-exploration` | No | No | Schema-only |
| `metadata-authoring` | No | No | Facet authoring |

Default mill-service MCP profile for chart workflows: **`data-analysis`** (same as Stage 1 SQL
execution).

---

## Documentation and test ownership

| Deliverable | Owner |
|-------------|-------|
| Update [`v3-mcp-capability-exposure.md`](../v3-mcp-capability-exposure.md) §15 inventory rows + tool counts | **WI-368** |
| `CapabilityMcpCatalogTest`: `data-analysis` lists `chart-mapping.*` | **WI-368** |
| HTTP MCP `testIT`: list + invoke chart tools (Skymill fixture, structural assertions) | **WI-368** or **WI-369** (mirror WI-341) |
| Example pointer in `misc/examples/ai-mcp-langchain-skymill/README.md` | **WI-368** story closure |

---

## Non-goals (MCP)

- Exposing mill-ui ECharts compile output over MCP
- MCP streaming of in-process `AgentEvent` / chat artifact SSE
- Separate **`chart-analysis`** MCP profile — use **`data-analysis`**
- Chart tools on **`schema-exploration`** without explicit profile change
