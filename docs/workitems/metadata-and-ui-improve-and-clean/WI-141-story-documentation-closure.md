# WI-141 — Story documentation closure

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Planned  
**Type:** docs  
**Area:** docs

## Summary

After implementation WIs on this branch are done (**through WI-130** unless otherwise agreed), update **architecture** and **user** documentation: **`FacetOrigin`**, multi-source read behaviour, merge / muting concepts, and what changed structurally in metadata/schema/UI.

Normative context: [`SPEC.md`](SPEC.md) §0, §3, **story closure** §8; [`RULES.md`](../RULES.md) (MILESTONE, BACKLOG, archive).

## Scope

### `docs/design/`

- Refresh or extend metadata design pages (e.g. [`metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md), [`facet-class-elimination.md`](../../design/metadata/facet-class-elimination.md)) so they match **shipped** behaviour: **captured vs inferred**, **`MetadataSource`**, **`originId`**, **`model`** root, merge placement (composition-first), repository vs read-source split.
- Document **architectural changes** introduced by this story (high level; link to code modules where useful).

### `docs/public/`

- **User-readable** explanation of **multi-source facets**: what users see (constellation, read-only inferred, **`originId`** affordance), difference between **captured** (editable when permitted) and **inferred** (read-only), and optional **`scope` / `origin`** filters in plain language — avoid type dumps as the main text.

## Out of scope

- Rewriting **SPEC** / **WI** files (already source of truth for implementers).

## Dependencies

- **WI-130** (or explicit sign-off that remaining implementation risk is acceptable for docs).

**Contingency:** If **WI-130** removes or renames types cited in current design docs (e.g. snapshot services), **expand this WI’s** `docs/design/` scope to **re-read the tree** and delete or rewrite sections that reference removed types — do not assume the outline above is exhaustive.

## Acceptance criteria

- Design docs accurately describe **FacetOrigin** and multi-source behaviour post-merge.
- Public doc page(s) give a non-developer overview; **`mkdocs`** or project doc build passes if applicable (**`make docs-build`** or repo standard).

## Testing

```bash
make docs-build
```

(If the repo uses another entry point, use that instead.)

## Commit

One logical `[docs]` commit; update [`STORY.md`](STORY.md), [`MILESTONE.md`](../MILESTONE.md), [`BACKLOG.md`](../BACKLOG.md) per **RULES**; clean tree.
