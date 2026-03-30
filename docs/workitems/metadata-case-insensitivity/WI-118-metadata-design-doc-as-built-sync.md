# WI-118 — Sync `docs/design/metadata` with as-built model

Status: `planned`  
Type: `docs`  
Area: `metadata`, `persistence`, `data`  
Story: [`STORY.md`](./STORY.md)

## Goal

After **WI-112** (up-front design) and the **implementation WIs** (**WI-113**–**WI-117**), update **`docs/design/metadata/`** so it describes the **actual shipped** metadata model: tables and relationships (including **`metadata_entity_type_*`**, facet def/inst, scopes, row-level audit columns, facet investigation audit), **URN grammar and binding registry** as implemented, **REST/import contracts**, and any **deltas** from the WI-112 draft (ERD or narrative, migration id, column renames).

## Relationship to WI-112

- **WI-112** — **Authoritative target** and decisions **before** / during build; may intentionally omit detail.
- **WI-118** — **As-built documentation** so future agents and operators are not misled by drift; run **last** on the story branch (or immediately before merge).

## In scope

- Refresh or add markdown under `docs/design/metadata/` (single index + topic files as needed).
- Align descriptions with Flyway squashed script, JPA entity names, and public APIs.
- Note dev DB reset / Flyway repair if relevant to operators.

## Out of scope

- Primary updates to **`docs/public/`** — owned by **WI-116**; **WI-118** verifies consistency / catches drift only if needed.
- Rewriting code comments (optional cross-links only).

## Code documentation (this WI)

- **Markdown only** for the as-built refresh. If the review surfaces **undocumented** production symbols added in **WI-113–WI-117**, either fix KDoc/JavaDoc in a small follow-up commit on the same branch or list gaps in the PR for the assignee to close before merge.

## Acceptance criteria

- A reviewer can derive **current** ERD-level understanding from `docs/design/metadata/` alone.
- **WI-118** checkbox in [`STORY.md`](./STORY.md) marked complete when merged.
- [`STORY.md`](./STORY.md) **Design & inventory doc checklist** — all boxes **`[x]`** or PR proves equivalent updates.
- No known missing **parameter-level** KDoc/JavaDoc on **non-test** APIs introduced by this story (spot-check against modules touched).

## Commit

One logical commit for this WI, prefix `[docs]`, per `docs/workitems/RULES.md`.
