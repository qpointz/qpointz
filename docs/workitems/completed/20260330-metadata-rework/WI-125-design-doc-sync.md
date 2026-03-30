# WI-125 — Story closure (metadata rework)

**Story:** Metadata Rework  
**Depends on:** WI-127 and WI-128 (documentation complete); WI-124 (code complete)

## Objective

Close the story branch: confirm documentation work from **WI-127** and **WI-128**, update tracking artefacts (**MILESTONE.md**, **BACKLOG.md**), and run a final **`mill.metadata.storage`** spot-check under **`docs/`**.

**Substantive documentation** is owned by:

- **[`WI-127-metadata-design-docs-domain-model.md`](./WI-127-metadata-design-docs-domain-model.md)** — design docs, domain model reference, design-side Spring config.
- **[`WI-128-metadata-public-user-docs.md`](./WI-128-metadata-public-user-docs.md)** — public site, user-facing metadata section, public Spring config.

## Scope

### 1. Verify WI-127 and WI-128

- [x] [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) is complete (not stub) and linked.
- [x] `docs/public/mkdocs.yml` includes Metadata nav; `mkdocs build` from `docs/public` (or `make docs-build` from repo root) succeeds.
- [x] Final spot-check: `rg 'mill\.metadata\.storage'` under `docs/` — no stale **normative** references (explicit “removed” / migration / historical notes in SPEC, PLAN, WI files, and **MILESTONE** are OK).

### 2. Tracking & durable record

- [x] Update [`docs/workitems/MILESTONE.md`](../../MILESTONE.md) — completed WIs for this story.
- [x] Update [`docs/workitems/BACKLOG.md`](../../BACKLOG.md) — related entries `done` where applicable.

### 3. Story folder

- [x] **Archive** to `docs/workitems/completed/20260330-metadata-rework/` — SPEC, STORY, PLAN, and WI files preserved per [`RULES.md`](../../RULES.md) (move, do not delete).

## Done Criteria

- WI-127 and WI-128 checklists satisfied (or listed gaps filed as follow-up outside this story).
- MILESTONE and BACKLOG updated.
- Story folder **archived** under `docs/workitems/completed/20260330-metadata-rework/`.
- Merging into `dev` is performed by the user.
