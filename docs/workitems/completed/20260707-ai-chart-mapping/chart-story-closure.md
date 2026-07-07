# Story closure and doc hygiene (ai-chart-mapping)

Normative for **Gap 28** / story archive. Complements [`RULES.md`](../../RULES.md).

## Locked decisions (Gap 28)

| Tracker / topic | When | Action |
|-----------------|------|--------|
| **`BACKLOG.md`** | **Story closure only** | Set related rows to **`done`**; add deferred follow-ups. Row **pruning** waits for **release** per `RULES.md`. |
| **`MILESTONE.md`** | **Not at story closure** | Updates only when preparing a **new version / release** (next `## x.y.z` draft block). Do not block WI-370 or story MR on MILESTONE edits. |
| **Design docs** (`docs/design/`) | **During development on branch** + review at closure | Keep aligned with shipped WIs (agentic/charts/*, artifact-foundation, chat-artefact-architecture, sql-query execution, etc.). |
| **Public docs** (`docs/public/src/`) | **During development on branch** + review at closure | User-facing mill-ui / AI chat behaviour for chart artifacts when WI-370 lands. |
| **Feature flag** | N/A | **Not required** — chart capability via profile composition; UI renders charts when wire artefacts exist (`general` chat). |
| **Branch name** | Informational | Reset branch is `restart/ai-chart-mapping-after-stage1`. |

## Story closure checklist (when user requests closure)

1. **MR-ready history** — squash/regroup commits per `RULES.md` § Completion (Story level).
2. **`BACKLOG.md`** — mark chart-mapping rows **`done`**.
3. **Design docs** — final pass: chart contract, charts/* gap specs, artifact wire, chat architecture.
4. **Public docs** — chart preview, Run, semantic artefacts (no ECharts in payload).
5. **Archive** — move story folder to `docs/workitems/completed/YYYYMMDD-ai-chart-mapping/`.
6. **Skip at closure** — `MILESTONE.md` (unless simultaneously cutting a release), feature flags.

## During active development (current branch)

Before each WI commit and before MR:

- Update **design** docs when behaviour changes (not only at closure).
- Update **public** docs when user-visible behaviour ships (especially WI-370).
- Keep **`GAPS.md`** / **`STORY.md`** / WI files in sync with locks and checkboxes.

## Out of scope

- Adding a `mill.features.*` flag for chart mapping or chart UI.
- Creating BACKLOG rows mid-story (optional; closure mark **`done`** is sufficient if rows exist).
