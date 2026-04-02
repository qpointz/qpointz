# Work Item Rules

These rules apply to **every** work item and story implementation.

## Stories

A **story** is a coherent unit of delivery that maps to a single Git branch merged into `dev`.
Stories group related work items and are the primary unit of planning.

### Story folder layout

Every story lives in its own subfolder under `docs/workitems/`:

```
docs/workitems/
  <story-slug>/          # active story; slugified topic, e.g. metadata-persistence
    STORY.md             # high-level objectives + ordered WI checklist (see below)
    WI-NNN-<title>.md    # individual work item files for this story
    WI-NNN-<title>.md
    ...
  completed/             # closed stories (moved here, not deleted)
    YYYYMMDD-<story-slug>/
      STORY.md
      WI-NNN-<title>.md
      ...
```

- **Folder name**: lowercase, hyphen-separated slug of the story topic.
- **`STORY.md`**: required at story creation. Must contain:
  - A short description of the story's goal.
  - An ordered checklist of all WIs in the story — this is the **tracking list** (live progress
    for the story branch):
    ```markdown
    ## Work Items
    - [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
    - [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
    ```
  - **As soon as a WI is finished:** update the tracking list (checkbox to `[x]` for that WI), update
    the WI’s `WI-NNN-<title>.md` if the story expects it (notes, acceptance, status), then **commit**
    that WI’s full working copy (see **Commits** below). Do not leave completed WIs unchecked or
    uncommitted while starting the next WI.
- **WI files**: all WI markdown files for the story live under the story folder, not at the top
  level of `docs/workitems/`.

### Story closure

When all WIs in a story are complete **and** the branch is **MR-ready** (rewritten history per
**Completion (Story level)** below — logical commit groups, reviewable for merge):

1. **Update `MILESTONE.md`** — record the story's completed WIs in the appropriate milestone
   section (use the same compact bullet format as existing entries).
2. **Update `BACKLOG.md`** — mark any related backlog entries as `done`.
3. **Update or create design docs** under the relevant `docs/design/<component>/` section
   (e.g. `agentic/`, `metadata/`, `platform/`, `security/`) — capture decisions, architecture
   notes, and anything a future agent or developer needs to understand the system. Design docs
   are organised by logical component, not by story.
4. **Update or create user documentation** under `docs/public/src/` — ensure the public-facing
   docs reflect any new features or changed behaviour.
5. **Archive the story folder** — do **not** delete it. Move the entire folder to:
   `docs/workitems/completed/YYYYMMDD-<story-slug>/`, where:
   - **`YYYYMMDD`** is the **story closure date** (UTC unless the team agrees otherwise —
     the calendar day the work is accepted as merge-ready or merged).
   - **`<story-slug>`** is the original active folder name (lowercase hyphen-separated slug).
   - Example: `docs/workitems/completed/20260330-metadata-persistence/`

   The archived folder preserves `STORY.md`, all WI files, and any other story-local artefacts
   as the historical record. Durable summaries remain in **MILESTONE**, **BACKLOG**, and
   **design / public docs** as today.

   **Ordering:** With a `YYYYMMDD-` prefix, **ascending** name sort lists **oldest** closures first.
   To see **most recent closures first**, sort folder names **descending** (reverse alphabetical)
   in your file browser, or maintain the index in `docs/workitems/completed/README.md`.

Merging into `dev` is done manually by the user; the agent prepares everything above first.

---

## Branching

- Each story is implemented on a **dedicated branch**. **Usually** it is created from `origin/dev`;
  it may legitimately branch from another integration point when a story depends on unmerged work —
  at **closure**, use the branch’s **actual merge target** (normally `origin/dev`) when rebasing and
  when deciding what to squash.
- Preferred when starting from mainline: `git fetch origin && git checkout -b <story-slug> origin/dev`.
- Individual WIs within a story share the same branch; they are **not** separated into sub-branches
  unless a WI explicitly depends on another that is not yet merged.
- Before the branch is ready for review, **rebase** against `origin/dev`:
  `git fetch origin && git rebase origin/dev`.
- Never commit directly to `dev`. Never reuse a previous story branch.

## Commits

- At the end of each work item, squash its changes into **one logical commit**.
- Keep commits minimal — ideally **one commit per WI**, labelled with the WI identifier.
- **Never** add `Co-Authored-By` or similar trailers to commit messages.
- Follow the bracketed prefix style: `[feat]`, `[fix]`, `[change]`, `[refactor]`, `[docs]`, `[wip]`.

### Per-WI cadence (story implementation)

While implementing a story on its branch, treat each WI as a closed loop:

1. **Finish the WI** — code, tests, and docs required by that WI.
2. **Update tracking** — set the matching item to `[x]` in `STORY.md`; update `WI-NNN-<title>.md` when
   the story or WI template calls for it.
3. **Commit** — stage everything that belongs to that WI (including tracking/doc edits) and create one
   commit so `git status` is clean before the next WI.

If you complete a WI but skip updating `STORY.md` or skip the commit, the branch no longer matches
the story’s tracking list and history is harder to review.

### Complete working copy per WI (story branches)

On a **story branch**, when a WI is **complete** (implementation + tests/docs as required by that WI):

1. **Commit every file** that is part of that WI’s delivery — source, tests, story docs (`STORY.md` tracking list / checkbox, WI file updates if any), and any other intentional edits. Do not stop with a **partial** commit while leaving related changes unstaged for the same WI.
2. **Leave a clean working tree** for that slice of work: after the commit, `git status` should show no remaining modified/untracked files **for completed work** (aside from deliberate local-only files such as IDE noise, if those are gitignored).
3. **Same commit** should normally include marking the WI as done in **`STORY.md`** (`[x]`) so the branch always reflects completed WIs and the tracking list stays accurate.

This keeps each WI a **reviewable, reproducible checkpoint** on the story branch. Do not accumulate multiple finished WIs worth of changes without committing. Do not commit build outputs, secrets, or unrelated work from outside the story.

## Completion (WI level)

- When the WI is implemented, update the **tracking list** in `STORY.md` (checkbox `[x]`) **in the same
  commit** as the WI’s code/docs — see **Per-WI cadence** and **Complete working copy per WI** above.
- Do **not** remove or relocate WI files until **story closure** — they stay with the story folder through its move to `docs/workitems/completed/`.

## UI Module Reference

The current Mill frontend is **`ui/mill-ui/`** — a React 19 + TypeScript + Vite application.

The legacy module `services/mill-grinder-ui` is **retired**. When any rule, WI, or instruction
references "UI", "the frontend", or "mill-ui", it always means `ui/mill-ui/`.
Design documents in `docs/design/ui/` may still reference `mill-grinder-ui` in historical
context; for new work the active module is `ui/mill-ui/`.

---

## Completion (Story level)

Before opening a merge request (or declaring the branch merge-ready), **rewrite the story branch
history** so reviewers can follow it. Per-WI commits are a convenience during implementation; **closure**
is when those commits are **grouped logically by the substance of the change** (feature area, module,
risk boundary, or a single WI when it stands alone), not necessarily one commit per WI on the final
branch.

1. **Choose the merge base** — normally `origin/dev`. If the story branched elsewhere, use the commit
   your MR will target (e.g. `git merge-base HEAD origin/dev` or the agreed integration branch).
2. **Squash and regroup interactively** — e.g. `git fetch origin && git rebase -i <merge-base>` (or
   `git rebase -i origin/dev` when that is the target). Combine fixups, split unrelated hunks if
   needed, and **order commits so each tells a coherent story** (readable `git log`, bisect-friendly
   where practical).
3. **MR-ready bar** — after rewrite, the branch should look like a deliberate sequence of changes:
   clear messages (bracket prefix, imperative, under 72 chars), no stray WIP noise, suitable for
   code review and CI. **Guideline:** aim for **at most about 10 commits** above the merge base; if
   more are needed for clarity (large stories, risky refactors), that is acceptable — prefer reviewable
   grouping over a hard number.
4. Follow the **Story closure** documentation steps above (MILESTONE, BACKLOG, design docs,
   public docs, archive story folder under `docs/workitems/completed/YYYYMMDD-<story-slug>/`).
