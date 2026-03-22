# Work Item Rules

These rules apply to **every** work item and story implementation.

## Stories

A **story** is a coherent unit of delivery that maps to a single Git branch merged into `dev`.
Stories group related work items and are the primary unit of planning.

### Story folder layout

Every story lives in its own subfolder under `docs/workitems/`:

```
docs/workitems/
  <story-slug>/          # slugified topic, e.g. metadata-persistence
    STORY.md             # high-level objectives + ordered WI checklist (see below)
    WI-NNN-<title>.md    # individual work item files for this story
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
5. **Delete the story folder** (`docs/workitems/<story-slug>/`) — the story's WI files and
   `STORY.md` are ephemeral; the above artefacts are the durable record.

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

## Completion (WI level)

- Mark the WI checkbox in `STORY.md` as `[x]` when the WI is implemented.
- Do **not** delete the WI file until story closure (it is needed for the story record).

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
   public docs, delete story folder).
