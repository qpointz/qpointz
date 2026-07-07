# WI-387 — Authoring examples and docs

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` |
| **Area** | `metadata`, `ai` |
| **Depends on** | [**WI-383**](WI-383-ai-annotation-facet-design.md)–[**WI-386**](WI-386-ai-annotation-sql-profile.md), [**WI-388**](WI-388-metadata-authoring-ai-annotation-capture.md) |
| **Enables** | Story closure prep (not closure itself) |

## Problem

Operators and agents need discoverable examples distinguishing **`ai-annotation`** from
**`descriptive`**, public docs for Model view behavior, and backlog traceability. Story closure
(MILESTONE, archive) waits for explicit user request per [`RULES.md`](../../RULES.md).

## Goal

Complete documentation and authoring seeds; add backlog rows **A-100** / **M-36**. Do **not**
archive the story or set backlog rows to `done` in this WI.

## Deliver

### 1. Authoring example seed

**File:** [`platform-facet-authoring-examples.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-authoring-examples.yaml)

Add `ai-annotation` example on `skymill.segments` (GAP-1): `title`, `instruction`, `kind: sql_generation`.

### 2. Public docs

**File:** [`docs/public/src/metadata/mill-ui.md`](../../../public/src/metadata/mill-ui.md)

Brief note under facet types: `ai-annotation` = entity-scoped agent instructions (MULTIPLE);
captured via chat when users define agent rules (**WI-388**); visible in Model view and on schema tools.

### 4. Design index (required — GAP-8)

Link [`ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md) from
[`docs/design/metadata/README.md`](../../../design/metadata/README.md).

### 5. Model view smoke check (GAP-6)

After WI-384 seed: metadata API or harness asserts `ai-annotation` assignment visible on
`skymill.segments` with `instruction` field (descriptor-driven; no custom UI WI).

### 6. Backlog rows (status `planned`)

**File:** [`BACKLOG.md`](../../BACKLOG.md)

| # | Item | Type | Status |
|---|------|------|--------|
| **A-100** | Entity-scoped `ai-annotation` facet: authoring capture, schema tool `aiAnnotations`, `data-analysis` SQL grounding (**WI-383**–**WI-386**, **WI-388**) | feature | `planned` |
| **M-36** | Platform `ai-annotation` facet type seed and design contract (**WI-383**, **WI-384**) | docs / feature | `planned` |

Link to [`planned/ai-annotations-facet/STORY.md`](STORY.md).

## Out of scope

- Story archive to `completed/`
- `MILESTONE.md` update
- Setting backlog rows to `done`

## Acceptance criteria

- Authoring example loads in metadata import / harness tests.
- Public mill-ui doc mentions `ai-annotation`.
- **A-100** and **M-36** rows present in BACKLOG with story link.
- Model view smoke check (GAP-6) passes on seeded `skymill.segments`.
- [`STORY.md`](STORY.md) tracker has WI-387 `[x]` when this WI completes.

## Deliverables

- This work item definition.
- Seeds, prompts, public docs, BACKLOG rows on the story branch.
