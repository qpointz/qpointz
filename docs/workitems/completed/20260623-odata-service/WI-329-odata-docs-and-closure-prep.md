# WI-329 — OData docs and closure prep

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** WI-328

## Goal

Public and platform doc sweep; story closure table (archive only on explicit user request).

## Scope

- `docs/public/src/data-access/odata.md` — live OData for Power BI / Tableau; RWS stack note; JDK 25 requirement
- `module-inventory.md`, `mill-data-lane-onepager.md`
- `odata-service.md` aligned with shipped RWS adapter pipeline
- `STORY.md` closure reconciliation table
- Document `com.sdl` **2.16.1** coordinates in `libs.versions.toml` (reference only in docs if not duplicated)

## Not in scope

- Archive story / MILESTONE / BACKLOG `done` until user requests closure

## Completion (normative)

After verify passes: update **tracker** (`STORY.md` `[x]`, this file status) → **commit** (include tracker in commit) → **push** → **CI green** → open **MR**. See [`STORY.md`](STORY.md) § Implementation delivery workflow.
