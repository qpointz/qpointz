# AI v3 — Schema exploration port, SQL validation implementation, and `mill-ai-v3-data`

**Location:** `docs/workitems/in-progress/ai-v3-schema-exploration-port/`

**Milestone:** **0.8.0**

**Goal:** Decouple **`mill-ai-v3`** from **`mill-data-schema-core`** by introducing an **AI-owned contract** for schema exploration tools (list schemas, tables, columns, relations) and moving the **`SchemaFacetService`**-based implementation into a new Gradle module **`mill-ai-v3-data`**. In the **same** story, add a **data-backed default `SqlValidator`** (engine/dialect-aligned validation) in **`mill-ai-v3-data`**, so **`validate_sql`** is not limited to mocks/ad hoc lambdas — while **`SqlQueryToolHandlers.validateSql`** and the **`SqlValidator` / `SqlValidationService` contracts** remain in **`mill-ai-v3`**.

**Rationale:** Today **`SchemaCapabilityDependency`** exposes **`io.qpointz.mill.data.schema.SchemaFacetService`** directly, and **`SchemaToolHandlers`** navigates Mill Data facet types (`WithFacets`, `RelationFacet`, …). That couples the merged runtime to the data layer. A **port** in **`mill-ai-v3`** keeps capability manifests, profiles, and **`SchemaExplorationAgent`** free of **`io.qpointz.mill.data.*`** imports; **`mill-ai-v3-data`** provides the **`SchemaFacetService` → port** adapter implementation.

**SQL validation:** The **`validate_sql`** tool pipeline is already split (**contract + thin `validateSql` in `mill-ai-v3`** per generate-only `sql-query` work). This story adds the **optional production implementation** of **`SqlValidator`** that can depend on **`mill-sql` / engine** code paths without pulling those dependencies into **`mill-ai-v3`** (**WI-165**).

**Wiring (target shape):** **Canonical** Spring wiring of **`SchemaExplorationPort`** from **`SchemaFacetService`** belongs in **`mill-ai-v3-autoconfigure`** (**WI-164**). **`SqlValidator` → `SqlValidationService`** bridging already exists (**`MillAiV3SqlValidatorAutoConfiguration`**); **WI-165** extends that story with a **default `SqlValidator` bean** from **`mill-ai-v3-data`** when appropriate. **Primary consumer** of that wiring in product code is **`mill-ai-v3-service`** (Boot + **`mill-ai-v3-autoconfigure`**). The **`mill-ai-v3-cli`** may **`implementation(project(":ai:mill-ai-v3-data"))`** only as a **temporary** playground / test-bench convenience for the standalone REPL (no Spring), until the CLI is thin HTTP-only or boots a minimal context that reuses autoconfiguration (**WI-163**).

**Execution order:** **WI-162** deliberately sequences **(A)** **`mill-ai-v3-data` Gradle module skeleton** (settings, minimal `build.gradle.kts`, compiles) **before (B)** the schema adapter, so **WI-165** and any **`testIT`** / autoconfigure work are not blocked on a monolithic commit. **WI-161** + **WI-162** remain a tight pair on the branch (contract + adapter); see **WI-162** for the A→B split.

**Scope:** **v3 only.** Does not change v1 NL-to-SQL. **Normative** capability behaviour (tool names, YAML output shapes) stays the same unless a WI explicitly updates manifests/tests.

**Relation to other stories:**

- **`WI-160`** ([`../../planned/ai-v3-chat-capability-dependencies/STORY.md`](../../planned/ai-v3-chat-capability-dependencies/STORY.md)) may still wire **collaborators** into **`AgentContext`**; after this story, those collaborators should prefer the **port** type (or an adapter bean) instead of **`SchemaFacetService`** in **`mill-ai-v3`** APIs — coordinate merge order if both branches touch **`SchemaCapabilityDependency`** / **`SchemaExplorationAgent`**.

## Work Items

- [x] WI-166 — Design documentation: `mill-ai-v3` / `mill-ai-v3-data` boundary (`WI-166-design-documentation-mill-ai-v3-data-boundary.md`)
- [x] WI-161 — `SchemaExplorationPort` contract in `mill-ai-v3`; remove `mill-data-schema-core` from `mill-ai-v3` (`WI-161-ai-v3-schema-exploration-port-contract.md`)
- [x] WI-162 — New `mill-ai-v3-data` module: `SchemaFacetService` adapter + moved handler logic (`WI-162-mill-ai-v3-data-module-and-adapter.md`)
- [x] WI-163 — `mill-ai-v3-cli`: temporary dependency on `mill-ai-v3-data`, thin glue, demo helpers, README (`WI-163-mill-ai-v3-cli-schema-port-wiring.md`)
- [x] WI-164 — `mill-ai-v3-autoconfigure`: register `SchemaExplorationPort` bean from `SchemaFacetService` (canonical wiring) (`WI-164-mill-ai-v3-autoconfigure-schema-exploration-port.md`)
- [x] WI-165 — `mill-ai-v3-data`: data-backed `SqlValidator` (engine/dialect validation) + autoconfigure default bean (`WI-165-mill-ai-v3-data-sql-validator-implementation.md`)

## Design references

- [`../../../design/agentic/v3-mill-ai-v3-data-boundary.md`](../../../design/agentic/v3-mill-ai-v3-data-boundary.md) — module split, wiring, **`mill-ai-v3-service`** vs CLI, SQL validation testing notes.
- [`../../../design/agentic/developer-manual/v3-developer-capabilities-profiles-and-dependencies.md`](../../../design/agentic/developer-manual/v3-developer-capabilities-profiles-and-dependencies.md) — capabilities and dependencies (**§1.1** data boundary, **`SqlValidator`** unit vs `testIT`).
- [`../../../design/agentic/README.md`](../../../design/agentic/README.md) — v3 runtime overview.
- [`../../../workitems/BACKLOG.md`](../../../workitems/BACKLOG.md) — **A-83** tracker row.
