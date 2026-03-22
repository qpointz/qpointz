# Story & Work-Item Workflow

This document describes the canonical process for organising work in this repository.
It is the authoritative reference for agents and contributors.

## Concepts

| Term | Definition |
|------|-----------|
| **Story** | A coherent delivery unit: one branch, one topic, merged into `dev` by the user. |
| **Work Item (WI)** | A discrete implementation task within a story. |
| **Story folder** | `docs/workitems/<story-slug>/` — ephemeral, deleted at story closure. |
| **Story slug** | Lowercase, hyphen-separated topic label (e.g. `metadata-persistence`). |

## Folder layout

```
docs/workitems/
  <story-slug>/
    STORY.md             # Goal + ordered WI checklist
    WI-NNN-<title>.md    # One file per work item
    WI-NNN-<title>.md
    ...
```

WI files live exclusively inside the story folder. Placing them at the top level of
`docs/workitems/` is not permitted.

## STORY.md structure

```markdown
# <Story Title>

<Short description of the story goal — one paragraph.>

## Work Items

- [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
- [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
```

Mark each checkbox `[x]` as the WI is implemented.

## Git branching

- One branch per story, branched from `origin/dev`.
- All WIs in the story share that branch.
- Sub-branches per WI are allowed only when a WI explicitly depends on unmerged prior work.
- Rebase against `origin/dev` before the branch is merge-ready.
- One squashed commit per WI; prefix: `[feat]`, `[fix]`, `[change]`, `[refactor]`, `[docs]`, `[wip]`.
- **Never** add `Co-Authored-By` trailers.

## Story closure checklist

Before signalling that the branch is ready to merge, the agent must:

0. **Squash and regroup commits** — review all commits since the branch diverged from `origin/dev`
   (`git log origin/dev..HEAD`) and squash/reorder them into a minimal set of logical commits
   (ideally one per WI) using `git rebase -i origin/dev`. The commit history must be clean and
   readable before merge.

1. **`docs/workitems/MILESTONE.md`** — record completed WIs in the appropriate milestone section.
2. **`docs/workitems/BACKLOG.md`** — mark related backlog items `done`.
3. **`docs/design/<component>/`** — update or create design docs in the relevant logical
   component section (e.g. `agentic/`, `metadata/`, `platform/`, `security/`). Design docs
   are organised by component, not by story; use the section that best matches the changed
   subsystem.
4. **`docs/public/src/`** — update or create user-facing documentation for any new or changed
   features.
5. **Delete `docs/workitems/<story-slug>/`** — the folder and all WI files are ephemeral; the
   artefacts above are the durable record.

Merging into `dev` is performed manually by the user after review.

## Deferred items

If work is identified during a story but deferred, document it in the relevant
`docs/design/<component>/` section and add a corresponding backlog entry in `BACKLOG.md`.

## Reference

- Detailed rules: `docs/workitems/RULES.md`
- Project conventions: `CLAUDE.md` (section "Stories, Work Items & Branching")
