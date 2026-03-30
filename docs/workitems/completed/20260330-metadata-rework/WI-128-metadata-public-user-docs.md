# WI-128 — Metadata public user documentation & public Spring config

**Story:** Metadata Rework  
**Spec sections:** [`SPEC.md`](./SPEC.md); **audience:** *User documentation* (analysts, operators, integrators) — not internal ADRs.  
**Depends on:** WI-124 (behaviour and keys stable). **Recommended after WI-127** so user pages can link to [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) where helpful.

## Objective

1. **Expose and expand** the **Metadata** section of the public docs site (`docs/public/`) at **user-documentation** depth.
2. **Update every `mill.metadata.*` (and related) mention** under **`docs/public/`** so it matches **`mill.metadata.repository.*`**, **`mill.metadata.seed.*`**, and removed **`mill.metadata.storage.*`**.

## Scope

### 1. Site navigation

- Add a **Metadata** entry to [`docs/public/mkdocs.yml`](../../../public/mkdocs.yml) **`nav`** (the `metadata/*.md` sources exist today but are **not** linked in `nav`).

### 2. User-facing content (`docs/public/src/`)

**Tone:** clear prose on what metadata **is** in Mill, how it shows up in the product, and what operators configure; use internal names sparingly (glossary OK).

**Tasks:**

- **New page** (recommended): [`docs/public/src/metadata/system.md`](../../../public/src/metadata/system.md) — e.g. **“Metadata in Mill”**: entities, facets, scopes, UI surfaces, YAML/import at concept level; link to [Concepts](concepts.md), [Operator guide](operators.md), and optionally the design reference [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) for readers who want depth.
- Refresh [`metadata/index.md`](../../../public/src/metadata/index.md), [`concepts.md`](../../../public/src/metadata/concepts.md), [`operators.md`](../../../public/src/metadata/operators.md), [`metadata/mill-ui.md`](../../../public/src/metadata/mill-ui.md).
- Update top-level [`mill-ui.md`](../../../public/src/mill-ui.md) and [`grinder-ui.md`](../../../public/src/grinder-ui.md) if they mention metadata URLs or configuration — keep aligned with the metadata section.

### 3. Spring configuration — public docs only

**Inventory:** `rg 'mill\.metadata|metadata\.(repository|storage|seed)' docs/public/src` — and any other normative config snippets under `docs/public/` (excluding generated `site/`).

**Update** all hits to as-implemented keys and semantics (repository type `jpa` \| `file` \| `noop`, file.*, seed list, no `storage.*`).

## Done Criteria

- [x] `docs/public/mkdocs.yml` includes a **Metadata** `nav` block listing the intended pages.
- [x] `mkdocs build` from `docs/public` (or `make docs-build`) succeeds.
- [x] No stale normative **`mill.metadata.storage.*`** under **`docs/public/src/`**.
- [x] One commit for **WI-128** per [`RULES.md`](../../RULES.md).

## Relation to other WIs

- **WI-127** — design docs + `mill-metadata-domain-model.md` + design/inventory Markdown.  
- **WI-125** — story closure after **WI-127** and **WI-128**.
