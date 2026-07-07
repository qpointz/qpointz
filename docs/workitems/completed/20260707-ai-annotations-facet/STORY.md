# AI annotations — entity-scoped agent instructions facet

**Status:** `closed` (**2026-07-07**)  
**Branch:** `feat/ai-annotations-facet` (MR !419)  
**Milestone:** **0.8.0**  
**Story folder:** [`docs/workitems/completed/20260707-ai-annotations-facet/`](.) — archived **2026-07-07**.  
**Related backlog:** **[A-101](../../BACKLOG.md)**, **[M-36](../../BACKLOG.md)** (`done`)

Introduce platform facet type **`ai-annotation`**: **MULTIPLE** scoped instructions on catalog
entities (`schema`, `table`, `attribute`) for agent-facing procedural guidance — distinct from
**`descriptive`** (single human catalog text) and **`relation`** (join structure).

**Example (Skymill):** table `segments` has `origin` / `destination` (city ids). Operators attach an
instruction: whenever this table is used in SQL, join `cities` and return **city names** instead of
raw ids unless the user explicitly asks for ids.

**Gaps:** [`GAPS.md`](GAPS.md)

Today only the **`schema`** capability projects entity facets into tool results, and only
**`descriptive`** on `list_schemas` / `list_tables` / `list_columns` (plus **`relation`** on
`list_relations`). This story extends the three descriptive-bearing `list_*` tools with
`aiAnnotations[]` and wires **`data-analysis`** / **`sql-query`** prompts to honor them.

**Design reference (WI-383):** [`docs/design/metadata/ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md)  
**Gaps (locked):** [`GAPS.md`](GAPS.md)  
**Tool mapping:** [`docs/design/metadata/schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md)

## Architectural decisions (locked)

| Decision | Choice |
|----------|--------|
| Facet URN | `urn:mill/metadata/facet-type:ai-annotation` |
| Category | `ai` |
| Cardinality | **MULTIPLE** |
| `applicableTo` | `schema`, `table`, `attribute` entity types |
| Payload | Manifest `contentSchema` only — no Kotlin `*Facet` lifecycle class |
| Agent exposure | **`schema.list_schemas`**, **`list_tables`**, **`list_columns`** only |
| Not extended | `list_relations`, `resolve_metadata_entity`, `metadata.*` catalog tools, `concept.*` |
| Authoring | **`metadata-authoring`** routes agent rules → **`ai-annotation`** capture (WI-388) |
| SQL consumption | **`sql-query`** honors `aiAnnotations` from schema tool output in-turn (no metadata port) |

## Capability tool extension inventory

| Tool | Extend? | Notes |
|------|---------|-------|
| `schema.list_schemas` | Yes | `aiAnnotations[]` from entity assignments |
| `schema.list_tables` | Yes | Primary — Skymill `segments` / `cities` case |
| `schema.list_columns` | Yes | Column-scoped rules |
| `schema.list_relations` | No | Structural `relation` facet only |
| `schema.resolve_metadata_entity` | No | URN resolution only |
| `metadata.*` | No | Facet type catalog, not per-entity effective data |
| `metadata-authoring.propose_facet_assignment` | **Yes (capture)** | Authors `ai-annotation`; WI-388 teaches when to use it vs `descriptive` |
| `concept.*` | No | Model-level concepts |
| `sql-query.*` | Indirect | Prompt guidance on schema tool output |

## Staged execution

1. **Design** — normative contract + tool inventory + authoring disambiguation (WI-383)
2. **Seed** — platform `FacetTypeDefinition` + Skymill `segments` fixture (WI-384)
3. **Authoring** — `metadata-authoring` capture routing for agent instructions (WI-388)
4. **Schema tools** — `aiAnnotations` on three `list_*` tools (WI-385)
5. **Profile** — `data-analysis` / `sql-query` prompts + scenario test (WI-386)
6. **Docs** — authoring examples, public docs, backlog rows (WI-387)

## Work Items

- [x] WI-383 — AI annotation facet design contract (`WI-383-ai-annotation-facet-design.md`)
- [x] WI-384 — Platform seed and Skymill fixture (`WI-384-ai-annotation-platform-seed.md`)
- [x] WI-388 — Metadata-authoring capture for agent instructions (`WI-388-metadata-authoring-ai-annotation-capture.md`)
- [x] WI-385 — Schema capability tool extension (`WI-385-ai-annotation-schema-tools.md`)
- [x] WI-386 — SQL profile integration and tests (`WI-386-ai-annotation-sql-profile.md`)
- [x] WI-387 — Authoring examples and docs (`WI-387-ai-annotation-docs-closure-prep.md`)

## Story acceptance

1. WI-383 design doc reviewed; tool extension inventory locked.
2. WI-384 loads `ai-annotation` from bootstrap; Skymill fixture on `segments`.
3. WI-388: user utterance defining agent SQL habits → `propose_facet_assignment` with **`ai-annotation`**, not **`descriptive`**.
4. WI-385: `schema.list_tables` for `skymill` returns `aiAnnotations` on `segments`; descriptive fields unchanged.
5. WI-386: `data-analysis` scenario produces SQL joining `cities` with city-name projection.
6. WI-387: authoring example + public docs; backlog **A-101** / **M-36** rows marked `done`.
7. Model view shows `ai-annotation` facet cards via descriptor-driven UI (no custom UI WI).

## Out of scope (story level)

- Parsing instructions into executable join plans (LLM interprets text)
- OData CSDL annotation mapping
- Global prompt injection of all annotations (in-turn / entity-scoped only)
- New dedicated tool (`get_ai_annotations`)
- `list_relations` / `metadata` catalog tool extension
- `ui/mill-grinder-ui` (legacy / retired)

## Related stories

- [`../completed/20260701-ai-concepts/STORY.md`](../completed/20260701-ai-concepts/STORY.md) — model-level `concept` facet + capability
- [`../completed/20260417-schema-capability-metadata/STORY.md`](../completed/20260417-schema-capability-metadata/STORY.md) — schema tools + descriptive mapping
- [`../completed/20260629-metadata-authoring-profiles/STORY.md`](../completed/20260629-metadata-authoring-profiles/STORY.md) — facet capture lifecycle
