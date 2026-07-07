# WI-388 — Metadata-authoring capture for agent instructions

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `feature` / `test` |
| **Area** | `ai` |
| **Depends on** | [**WI-383**](WI-383-ai-annotation-facet-design.md), [**WI-384**](WI-384-ai-annotation-platform-seed.md) — `ai-annotation` in catalog for `get_facet_type` |
| **Enables** | Story acceptance (authoring path); pairs with [**WI-386**](WI-386-ai-annotation-sql-profile.md) read path |

## Problem

[`metadata-authoring.yaml`](../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml) teaches the LLM to capture **`descriptive`**, data-quality, and **relation** facets, but has **no routing** for utterances where the user defines **rules for the agent itself** (SQL habits, join/projection behavior, tool-output conventions).

Those utterances are often misclassified as **`descriptive`** (`description` field) or answered in prose without **`propose_facet_assignment`**. Example: *“When you use the segments table, always join cities and show names not ids”* must become **`ai-annotation`** on `skymill.segments`, not a descriptive blurb.

## Goal

Update **`metadata-authoring`** prompts (and **`data-analysis`** profile composition where needed) so the LLM **recognizes agent-instruction intent** and captures **`facetTypeKey=ai-annotation`** via the standard facet-assignment lifecycle.

## Deliver

### 1. Facet-type disambiguation (normative — also in WI-383 design doc)

| User intent | Facet type | Counter-example |
|-------------|------------|-----------------|
| Catalog title, business description, tags for humans | **`descriptive`** | “Segments stores flight legs between cities” |
| **How the agent should behave** when using this entity in SQL/tools | **`ai-annotation`** | “When querying segments, join cities and return names instead of ids” |
| Declarative FK / join structure | **`relation-*`** | “segments.origin → cities.id” |
| Column must not be null / DQ rule | **`dq-*`** | “origin must not be null” |
| Model-wide business concept | **`concept`** on `model-entity` | “VIP passenger definition spans tables” |

**Signal phrases** (non-exhaustive) for **`ai-annotation`**: *when you query / when this table is used / always join / the agent should / for SQL / instead of ids show / must be joined to / every time this column appears*.

### 2. `metadata-authoring.yaml` prompt updates

**File:** [`capabilities/metadata-authoring.yaml`](../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml)

| Prompt | Change |
|--------|--------|
| `metadata-authoring.intent` | **AUTHOR_FACET** includes agent instructions and procedural rules for schema objects, not only descriptions and DQ |
| `metadata-authoring.reasoning` | Step 2/5: when user defines **agent behavior** on a grounded entity → shortlist **`ai`** category / **`ai-annotation`**; do **not** put imperative agent rules in **`descriptive.description`** |
| `metadata-authoring.batch` | Example row: `skymill.segments` + **`ai-annotation`** (cities join / city-name projection) alongside descriptive or DQ if also stated |
| `metadata.faceting.lifecycle` | List **`ai-annotation`** with other facet types; same accept/reject lifecycle |
| `metadata-authoring.payload-style` | **`instruction`** field: preserve imperative agent directives; rephrase for clarity without weakening “always/must/when” semantics; optional kebab-case **`title`** slug |
| `metadata.faceting.request` | Explicit: agent rules → **`ai-annotation`**, catalog descriptions → **`descriptive`** |
| `propose_facet_assignment` (tool `description`) | Examples include **`ai-annotation`**; facetTypeKey enum text lists `ai-annotation` |

### 3. Profile-level routing (`data-analysis`)

**File:** [`profiles/platform-agent-profiles.yaml`](../../../ai/mill-ai/src/main/resources/profiles/platform-agent-profiles.yaml)

Extend **`data-analysis.intent`** (or equivalent profile prompt) so mixed turns that document **agent SQL habits** on a table/column route to **`metadata-authoring`** / **AUTHOR_FACET**, not prose-only chat — without stealing **DATA_QUERY** from **`sql-query.intent`**.

### 4. Tests

| Test | Assert |
|------|--------|
| Prompt content / manifest test | Updated prompts mention **`ai-annotation`** and disambiguation from **`descriptive`** |
| Unit test (pattern: [`ConceptAuthoringCaptureTest`](../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/capabilities/concept/ConceptAuthoringCaptureTest.kt)) | Harness invokes `propose_facet_assignment` with `facetTypeKey=ai-annotation`, grounded `catalogPath`, payload with **`instruction`** |
| Scenario pack (optional, preferred) | User documents segments/cities rule → facet-assignment capture artefact with **`ai-annotation`**, not **`descriptive`** |

## Out of scope

- New capture tools (reuse **`propose_facet_assignment`**)
- **`metadata`** QUERY tool changes
- Auto-capture without user-facing accept/reject card

## Acceptance criteria

- Prompts distinguish **agent instructions** → **`ai-annotation`** vs **catalog narrative** → **`descriptive`**.
- Skymill-style utterance produces **`propose_facet_assignment`** with `facetTypeKey=ai-annotation` on the correct `catalogPath`.
- User still gets standard facet-proposal accept/reject in chat.
- No regression on existing descriptive / dq-null-check capture scenarios.

## Deliverables

- This work item definition.
- `metadata-authoring.yaml` (+ profile prompt if needed) and tests on the story branch.
