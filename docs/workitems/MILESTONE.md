# Milestones

**Draft release notes.** Treat this file as the **working draft** of **[`releases/RELEASE-x.y.z.md`](releases/)** for the **next** version only: the **`## x.y.z`** block below is pre-release material to be **promoted and polished** into **`RELEASE-x.y.z.md`** when git tag **`vx.y.z`** is cut. It must **not** retain sections for **already shipped** versions — those live only under **`releases/`** (for example **[`releases/RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md)** for **`v0.8.0`**). After tagging, **`MILESTONE.md`** is reset to the **following** milestone draft only — see [`RULES.md`](RULES.md) § **Milestone ledger (`MILESTONE.md`)** and § **Release (version) process**.

**Baseline (work since last shipped tag):** **`v0.8.0`** — e.g. `git log v0.8.0..HEAD`.

## 0.9.0

**Target date:** TBD — **not released.** Companion stub: create [`releases/RELEASE-0.9.0.md`](releases/RELEASE-0.9.0.md) when the draft stabilizes. **§ Completed** below = changes merged on `dev` **after** **`v0.8.0`** until **`v0.9.0`** is cut; narrative for **`v0.8.0`** is in **[`releases/RELEASE-0.8.0.md`](releases/RELEASE-0.8.0.md)** only.

### Completed

- **analysis-inline-chat-foundation** — Reusable inline chat + **Analysis copilot** (**WI-389**–**WI-407**): `analysis-copilot` profile, per-turn `context.values`, inline artifact strips/pills, explicit Apply/Apply & Run, copilot automation modes, host session binding, General Chat-style composer, resizable split drawer; design [`INLINE-CHAT-FOUNDATION.md`](../design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md); archive [`completed/20260709-analysis-inline-chat-foundation/`](completed/20260709-analysis-inline-chat-foundation/STORY.md). **Deferred:** artifact publish validation (**A-98**), context relations / UI presence persistence (**U-19**).

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items not yet delivered (still **planned** or **backlog** elsewhere). Carried forward from the **0.8.0** draft at release housekeeping.

- **Context relations platform** — generic related-object persistence, read API, inline chat UI restore (**WI-408**+); [`planned/context-relations/STORY.md`](planned/context-relations/STORY.md) (**U-19**)
- **Deterministic artifact publish validation** — runtime publish gate (**WI-397**–**WI-399**); [`planned/artifact-publish-validation/STORY.md`](planned/artifact-publish-validation/STORY.md) (**A-98**)
- **Flow scan pushdown** — Parquet column projection + filter pushdown (**WI-312**, **WI-313**); [`planned/flow-scan-pushdown/STORY.md`](planned/flow-scan-pushdown/STORY.md) (**D-10**)
- **Metadata value mapping bridge** — facet bridge, API, UI (**WI-171**–**WI-173**); [`planned/metadata-value-mapping/STORY.md`](planned/metadata-value-mapping/STORY.md) (**M-1**–**M-9**)
- **Concept object relations** — protocol, events, read UI (**WI-373**–**WI-377**); [`planned/concept-object-relations/STORY.md`](planned/concept-object-relations/STORY.md)
- **Metadata complex types** — structural facets + UI (**WI-034**); [`planned/metadata-complex-types/WI-034-metadata-complex-type-support.md`](planned/metadata-complex-types/WI-034-metadata-complex-type-support.md) (**M-27**)
- **Eliminate entity kind** — typed URN cleanup (**WI-144**); [`planned/eliminate-entity-kind/STORY.md`](planned/eliminate-entity-kind/STORY.md) (**M-34**)
- **WebFlux migration + method security** — (**WI-220**–**WI-228**); [`planned/webflux-migration-and-method-security/STORY.md`](planned/webflux-migration-and-method-security/STORY.md) (**P-34**)
- **mill-ui visual analysis modes** — (**WI-267**–**WI-272**); [`planned/mill-ui-visual-analysis-modes/STORY.md`](planned/mill-ui-visual-analysis-modes/STORY.md) (**U-14**)
- **AI v3 chat service documentation** — (**WI-084**); [`planned/ai-v3/WI-084-ai-v3-chat-service-documentation.md`](planned/ai-v3/WI-084-ai-v3-chat-service-documentation.md) (**A-82**)
- **ibis dialect validation** — (**WI-023**); [`planned/ibis-dialect-validation/WI-023-ibis-dialect-correctness-validation.md`](planned/ibis-dialect-validation/WI-023-ibis-dialect-correctness-validation.md) (**C-22**)
- **stdio MCP bridge** — descoped from MCP POC (**A-96**); [`backlog/WI-328-mill-ai-mcp-transport-stdio.md`](backlog/WI-328-mill-ai-mcp-transport-stdio.md)

**Note:** **WI-082** (mill-ui unified chat) was **delivered** in **0.8.0** via **WI-229**–**WI-233** (**U-11**); the planned WI file remains for historical reference only.
