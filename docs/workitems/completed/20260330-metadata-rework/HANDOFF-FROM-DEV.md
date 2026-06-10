# Starting implementation from `origin/dev` — branch topology

This note complements [`PLAN.md`](./PLAN.md) and [`STORY.md`](./STORY.md). It answers: *after rebasing, or if I branch from `origin/dev`, do I still have everything to implement?*

## What `git` inspection showed (branch `feat/metadata-rework-final`)

- **`origin/dev` does not contain** `docs/workitems/completed/20260330-metadata-rework/SPEC.md` (or the rest of this story folder) — the story was added in commit **`ee241b851`**.
- **`origin/dev` does not contain** `test/datasets/convert_to_canonical_yaml.py` nor the `*-meta-canonical.yaml` examples — those were added in commit **`9017952c5`** (along with large doc/code churn).
- **`origin/dev` does not contain** `docs/design/metadata/metadata-canonical-yaml-spec.md` (and related URN/canonical design updates from that same line of work).

Commits **ahead** of `origin/dev` on the sample branch (newest last):

| Commit     | Summary (short) |
|------------|-----------------|
| `673515c79` | Persist metadata entity ids / coordinates lowercase |
| `b372e892d` | Metadata URN platform story WIs / backlog |
| `204b4383c` | Model view MULTIPLE facets / facetPayloadUtils |
| `9017952c5` | Mill UI fixes close + **canonical YAML datasets**, design + metadata code |
| `ee241b851` | **Metadata rework story folder** (SPEC, STORY, PLAN, WI-119–128), PLAN handoff, small design tweaks |

## Scenario A — Rebase **this** branch onto `origin/dev`

You **keep** all commits (possibly reordered). You still have:

- Full **story folder** (SPEC, STORY, PLAN, WIs).
- **`test/datasets/`** canonical examples (from `9017952c5`).
- Any **code** changes from earlier commits.

**Conclusion:** **No extra snapshot is required** for completeness; Git already carries every file. The [`reference/`](./reference/) copies are **redundant insurance** (e.g. for reviewers who read only the story folder).

## Scenario B — **New branch from `origin/dev`** with only the story doc commit

If you **`git cherry-pick ee241b851`** only:

- You get **`docs/workitems/completed/20260330-metadata-rework/`** (including SPEC/PLAN/WIs) **plus** the small **`docs/design/metadata`** edits in that commit.
- You **do not** get `test/datasets/*` or `metadata-canonical-yaml-spec.md` unless you also cherry-pick **`9017952c5`** (heavy) or restore paths manually.

**Conclusion:** Use **[`reference/`](./reference/)** in this folder — it mirrors the dataset script, both canonical YAML samples, and the two design docs listed in [`reference/README.md`](./reference/README.md). Optionally run:

```bash
# Restore canonical examples into the tree expected by SPEC §11 links
mkdir -p test/datasets/skymill test/datasets/moneta
cp docs/workitems/completed/20260330-metadata-rework/reference/canonical-datasets/convert_to_canonical_yaml.py test/datasets/
cp docs/workitems/completed/20260330-metadata-rework/reference/canonical-datasets/skymill-meta-canonical.yaml test/datasets/skymill/
cp docs/workitems/completed/20260330-metadata-rework/reference/canonical-datasets/moneta-meta-canonical.yaml test/datasets/moneta/
```

```bash
# Optional: restore design copies (WI-127 will reconcile with as-built)
cp docs/workitems/completed/20260330-metadata-rework/reference/design/metadata-canonical-yaml-spec.md docs/design/metadata/
cp docs/workitems/completed/20260330-metadata-rework/reference/design/metadata-urn-platform.md docs/design/metadata/
```

## Scenario C — Implementation **code** only on `origin/dev`

The **specification** for the greenfield rework lives entirely under **`docs/workitems/completed/20260330-metadata-rework/`** once that commit is present. You do **not** need the older exploratory metadata commits on the branch to **read** the plan — but you **do** need either those commits or the **`reference/`** snapshots for **canonical YAML examples** and the **canonical YAML design** doc unless you recreate them.

## Summary

| Approach | Story SPEC/STORY/WIs | `test/datasets` + canonical design doc |
|----------|----------------------|------------------------------------------|
| Rebase full feature branch | Yes (in Git history) | Yes (in Git history) |
| Cherry-pick `ee241b851` only | Yes | **No** → use [`reference/`](./reference/) or cherry-pick `9017952c5` |
| Cherry-pick `9017952c5` then `ee241b851` | Yes | Yes (but pulls large unrelated diff) |

**Recommended:** Rebase the **full** branch onto `origin/dev` for implementation; keep [`reference/`](./reference/) as a **portable snapshot** for anyone who only has the story folder checked out or cherry-picks docs alone.
