# Gaps and open decisions — AI annotations facet

**Story:** [`STORY.md`](STORY.md)  
**Branch:** `feat/ai-annotations-facet`  
**Status:** planning review (2026-07-07)

Resolved items move into WI acceptance criteria, [`STORY.md`](STORY.md), or
`docs/design/metadata/ai-annotation-facet-type.md`, then are marked **LOCKED** here.

---

## Gap tracker

| ID | Title | Status | Priority | Owner WI |
|----|-------|--------|----------|----------|
| GAP-1 | Skymill canonical scenario naming | **locked** | **critical** | WI-384, WI-385, WI-386 |
| GAP-2 | `enabled` semantics (stored vs tool wire) | **locked** | high | WI-383, WI-385 |
| GAP-3 | Multiple annotations — order and conflicts | **locked** | medium | WI-383, WI-386 |
| GAP-4 | Scope / inheritance (exact entity only) | **locked** | high | WI-383, WI-385 |
| GAP-5 | Prompt precedence vs user turn / relations / SQL | **locked** | high | WI-383, WI-386 |
| GAP-6 | Model view acceptance verification owner | **locked** | low | WI-387 |
| GAP-7 | Skymill fixture seed path | **locked** | high | WI-384 |
| GAP-8 | Design index link required | **locked** | low | WI-387 |
| GAP-9 | Facet slug / URN / tool field names | **locked** | high | STORY, WI-383 |
| GAP-10 | Schema tool extension set (three `list_*` only) | **locked** | high | WI-385 |
| GAP-11 | Authoring vs `descriptive` disambiguation | **locked** | high | WI-388 |
| GAP-12 | Milestone / backlog rows | **locked** | low | WI-387 |

---

## 1. Skymill canonical scenario naming — **LOCKED**

**Gap (original):** Story prose used simplified names (`segment`, `airports`, `origin_id`) while
physical Skymill and [`SchemaFacetServiceSkyMillIT`](../../../../data/mill-data-schema-core/src/testIT/kotlin/io/qpointz/mill/data/schema/SchemaFacetServiceSkyMillIT.kt)
use **`segments`**, **`cities`**, columns **`origin`** / **`destination`**.

### Locked decision (2026-07-07)

**Use real Skymill catalog names everywhere** — story prose, fixtures, tests, and authoring examples.

| Item | Canonical value |
|------|-----------------|
| Schema | `skymill` |
| Fact table | `segments` |
| Lookup table | `cities` (not `airports`) |
| FK columns | `origin`, `destination` (reference `cities.id`) |
| Name projection | `cities.city` (and related descriptive columns), not raw ids |

**Reference instruction (fixture payload):**

```text
When this table is used in SQL, join skymill.cities twice for origin and destination
(origin and destination) and prefer city names in SELECT output instead of raw ids,
unless the user explicitly requests ids.
```

**Assignment target:** `urn:mill/model/table:skymill.segments`

**Owner:** WI-384 (fixture); WI-385 / WI-386 (assertions); update all story/WI prose to match.

---

## 2. `enabled` semantics — **LOCKED**

**Gap (original):** Optional `enabled` in payload vs mill-ui boolean defaults; tool wire shape unclear.

### Locked decision (2026-07-07)

| Layer | Rule |
|-------|------|
| **Stored payload** | `enabled` optional; **missing ⇒ `true`** at read/mapper time (same spirit as DQ `enabled`) |
| **Agent surfaces** | Rows with `enabled: false` are **omitted** from `aiAnnotations[]` |
| **Tool wire shape** | Emit only enabled instructions; **do not** include `enabled` on wire items (redundant once filtered) |
| **mill-ui forms** | Follow existing descriptor behavior; operators may set `enabled: false` explicitly in Expert JSON/YAML |

**Owner:** WI-383 (design); WI-385 (mapper)

---

## 3. Multiple annotations — order and conflicts — **LOCKED**

**Gap (original):** MULTIPLE cardinality without ordering or conflict rules.

### Locked decision (2026-07-07)

| Rule | Detail |
|------|--------|
| **Order** | Stable **`facetsResolved` merge order** (same order metadata read merge returns for that entity) |
| **Duplicates** | **Allowed** — no server-side deduplication in v1 |
| **Conflicts** | No automatic merge; prompts instruct agent to apply **all** enabled instructions unless the **current user message** explicitly contradicts one (see GAP-5) |
| **Capture** | Multiple user-stated rules → **multiple** `propose_facet_assignment` calls (parallel when independent) |

**Owner:** WI-383; WI-386 (prompt wording)

---

## 4. Scope and inheritance — **LOCKED**

**Gap (original):** Whether schema/table annotations inherit to child entities in tool output.

### Locked decision (2026-07-07)

**Exact entity only** — same rule as **`descriptive`** today.

| Tool row | `aiAnnotations` source |
|----------|------------------------|
| `list_schemas` | Assignments on **schema** entity only |
| `list_tables` | Assignments on **table** entity only |
| `list_columns` | Assignments on **attribute** entity only |

No upward or downward inheritance in v1. To affect columns, assign at column level (or document
table-level habit on the table assignment consumed when `list_tables` is called).

**Owner:** WI-383; WI-385

---

## 5. Prompt precedence — **LOCKED**

**Gap (original):** How `ai-annotation` interacts with user turn, relations, SQL validity.

### Locked decision (2026-07-07)

Precedence (highest wins first):

1. **Hard constraints** — SQL dialect validity, authorization, safety (always)
2. **Explicit user request in the current turn** — e.g. user asks for raw ids → override name-projection instruction
3. **`ai-annotation`** instructions with `kind=sql_generation` (or default) on entities returned in the same turn
4. **`relation`** join metadata and **`descriptive`** catalog text — structural / narrative context, not procedural overrides
5. **Model-level `concept`** facets — separate capability; do not fold into per-entity `aiAnnotations`

Normative phrase for prompts: *“Apply enabled `aiAnnotations` unless the user’s current message explicitly requests otherwise.”*

**Owner:** WI-383; WI-386

---

## 6. Model view acceptance owner — **LOCKED**

**Gap (original):** Story acceptance mentions Model view but no WI owned verification.

### Locked decision (2026-07-07)

**WI-387** includes a smoke assertion: after WI-384 seed, Model view / metadata API lists
`ai-annotation` assignments on a seeded entity with descriptor-driven fields (`instruction`, optional
`title`). No dedicated UI WI; no screenshot requirement unless product asks later.

**Owner:** WI-387

---

## 7. Skymill fixture seed path — **LOCKED**

**Gap (original):** WI-384 left fixture location TBD.

### Locked decision (2026-07-07)

Add the Skymill **`ai-annotation`** assignment to
[`test/datasets/skymill/skymill-meta-seed-canonical.yaml`](../../../../test/datasets/skymill/skymill-meta-seed-canonical.yaml)
— already loaded by `SchemaFacetServiceSkyMillIT` via `mill.metadata.seed.resources[1]`.

Do **not** introduce a parallel seed file unless merge conflicts force a split.

**Owner:** WI-384

---

## 8. Design index link — **LOCKED**

**Gap (original):** WI-387 marked README link optional.

### Locked decision (2026-07-07)

**Required:** link [`ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md)
from [`docs/design/metadata/README.md`](../../../design/metadata/README.md) in WI-387.

**Owner:** WI-387

---

## 9. Facet slug / URN / tool field names — **LOCKED**

### Locked decision (2026-07-07)

| Item | Value |
|------|--------|
| Facet URN | `urn:mill/metadata/facet-type:ai-annotation` |
| Catalog key | `ai-annotation` |
| Tool field | `aiAnnotations` (array on `list_schemas` / `list_tables` / `list_columns` items) |
| Wire item fields | `title`, `instruction`, `kind`, `tags` (no `enabled` on wire — GAP-2) |

**Owner:** STORY; WI-383

---

## 10. Schema tool extension set — **LOCKED**

### Locked decision (2026-07-07)

Extend **only** the three tools that already project **`descriptive`**:

- `schema.list_schemas`
- `schema.list_tables`
- `schema.list_columns`

**Not** extended: `list_relations`, `resolve_metadata_entity`, `metadata.*`, `concept.*`, new tools.

**Owner:** WI-385

---

## 11. Authoring vs `descriptive` — **LOCKED**

### Locked decision (2026-07-07)

User utterances that define **agent / SQL behavior** on a grounded entity → capture
**`ai-annotation`**, not **`descriptive.description`**.

Catalog narrative (what the entity *is*) → **`descriptive`**.

**Owner:** WI-388

---

## 12. Milestone and backlog — **LOCKED**

### Locked decision (2026-07-07)

| Item | Value |
|------|--------|
| Milestone | **0.8.0** (draft on story/WIs) |
| Backlog | **A-100** (AI injection), **M-36** (metadata facet type) — add in WI-387 as `planned` |
| Closure | Do not mark `done` / archive until explicit story closure per [`RULES.md`](../../RULES.md) |

**Owner:** WI-387

---

## Resolution log

| Date | ID | Decision |
|------|-----|----------|
| 2026-07-07 | GAP-1 | Real Skymill names: `segments`, `cities`, `origin`/`destination`; fixture on `skymill.segments` |
| 2026-07-07 | GAP-2 | Missing `enabled` ⇒ true; omit disabled from wire; no `enabled` on wire items |
| 2026-07-07 | GAP-3 | Merge order; duplicates allowed; user turn overrides conflicts |
| 2026-07-07 | GAP-4 | Exact entity only; no inheritance |
| 2026-07-07 | GAP-5 | Precedence: hard constraints > user turn > ai-annotation > relation/descriptive |
| 2026-07-07 | GAP-6 | WI-387 smoke check for Model view |
| 2026-07-07 | GAP-7 | `test/datasets/skymill/skymill-meta-seed-canonical.yaml` |
| 2026-07-07 | GAP-8 | README link required |
| 2026-07-07 | GAP-9–12 | Locked with STORY / WI owners |
