# Work Item Rules

These rules apply to **every** work item implementation.

## Branching

- Each work item is implemented on a **dedicated branch**.
- Preferred: branch from `origin/dev` via `git fetch origin && git checkout -b <branch-name> origin/dev`.
- Allowed: branch from the last work item's branch if it depends on prior work.
- Before pushing, the branch **must be rebased** against `origin/dev`: `git fetch origin && git rebase origin/dev`.
- Never commit directly to `dev`. Never reuse a previous work-item branch.

## Commits

- At the end of a work item, all commits **must be squashed** into logical commits.
- Keep the number of commits to the bare minimum — ideally **one commit per work item**.
- **Never** add `Co-Authored-By` or similar trailers to commit messages.
- Follow the existing bracketed prefix style: `[feat]`, `[fix]`, `[change]`, `[refactor]`, `[docs]`, `[wip]`.

## Completion

- When a work item is implemented, **delete its file** from `docs/workitems/`.
- Update `MILESTONE.md` to reflect the completed item in the **Completed** section.
- Update `BACKLOG.md` to mark any related backlog entries as `done`.
