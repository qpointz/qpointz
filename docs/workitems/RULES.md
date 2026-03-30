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
  - An ordered checklist of all WIs in the story (used as the live progress tracker):
    ```markdown
    ## Work Items
    - [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
    - [ ] WI-NNN — Short title (`WI-NNN-<title>.md`)
    ```
  - Update each checkbox to `[x]` as each WI is completed.
- **WI files**: all WI markdown files for the story live under the story folder, not at the top
  level of `docs/workitems/`.

### Story closure

When all WIs in a story are complete and the branch is ready to merge:

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

- Each story is implemented on a **dedicated branch** branched from `origin/dev`.
- Preferred: `git fetch origin && git checkout -b <story-slug> origin/dev`.
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

### Complete working copy per WI (story branches)

On a **story branch**, when a WI is **complete** (implementation + tests/docs as required by that WI):

1. **Commit every file** that is part of that WI’s delivery — source, tests, story docs (`STORY.md` checkbox, WI file updates if any), and any other intentional edits. Do not stop with a **partial** commit while leaving related changes unstaged for the same WI.
2. **Leave a clean working tree** for that slice of work: after the commit, `git status` should show no remaining modified/untracked files **for completed work** (aside from deliberate local-only files such as IDE noise, if those are gitignored).
3. **Same commit** should normally include marking the WI as done in **`STORY.md`** (`[x]`) so the branch always reflects completed WIs.

This keeps each WI a **reviewable, reproducible checkpoint** on the story branch. Do not accumulate multiple finished WIs worth of changes without committing. Do not commit build outputs, secrets, or unrelated work from outside the story.

## Completion (WI level)

- Mark the WI checkbox in `STORY.md` as `[x]` when the WI is implemented (typically **in the same commit** as the WI’s code/docs — see **Complete working copy per WI** above).
- Do **not** remove or relocate WI files until **story closure** — they stay with the story folder through its move to `docs/workitems/completed/`.

## UI Module Reference

The current Mill frontend is **`ui/mill-ui/`** — a React 19 + TypeScript + Vite application.

The legacy module `services/mill-grinder-ui` is **retired**. When any rule, WI, or instruction
references "UI", "the frontend", or "mill-ui", it always means `ui/mill-ui/`.
Design documents in `docs/design/ui/` may still reference `mill-grinder-ui` in historical
context; for new work the active module is `ui/mill-ui/`.

---

## Completion (Story level)

Before signalling the branch is merge-ready:

1. **Squash and regroup commits** — review all commits on the branch since it diverged from
   `origin/dev` (`git log origin/dev..HEAD`) and squash/reorder them into a minimal set of
   logical commits. Each commit should represent one coherent change (typically one WI).
   Use `git rebase -i origin/dev` to squash interactively.
2. Follow the **Story closure** documentation steps above (MILESTONE, BACKLOG, design docs,
   public docs, archive story folder under `docs/workitems/completed/YYYYMMDD-<story-slug>/`).
