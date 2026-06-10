# Mill UI — known facet field stereotypes

**Status:** Implementation reference (`ui/mill-ui`)  
**Last updated:** 2026-03-30  
**Related:** [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md), [`model-view-facet-boxes.md`](model-view-facet-boxes.md), [`metadata-canonical-yaml-spec.md`](metadata-canonical-yaml-spec.md) §11

---

## Purpose

Facet type **payload** fields may carry an optional **`stereotype`**: presentation hints for **Mill UI** (Data Model facet panels and facet-type admin). The **metadata service does not interpret** stereotypes; they are advisory tags only.

This document lists **known stereotypes** — tags that **`mill-ui` recognises** and maps to specialised controls, validation, or read-only layouts. Additional tags may be stored on descriptors (admin **Stereotype** pills accept free text) but **fall back** to generic schema-driven widgets until explicitly handled in code.

**Authoritative implementation:** `ui/mill-ui/src/utils/facetStereotype.ts` and call sites in `ui/mill-ui/src/components/data-model/EntityDetails.tsx` (read/edit) and `FacetTypeEditPage.tsx` (admin wiring only).

---

## Wire shape (recap)

Per field, `stereotype` is either:

- A **comma-separated string** of tags when the field’s value schema is **not** `ARRAY`, or  
- A **JSON array of strings** when the value schema is `ARRAY` (each tag applies to presentation of that array field as a whole).

Tags are compared **case-insensitively** after normalisation (trim; see `stereotypeTagsFromWire`).

---

## Tag precedence (STRING / ARRAY-of-STRING only)

For value schemas where **string stereotypes** apply (`STRING`, or `ARRAY` whose `items` are `STRING`):

| Condition | Effective string presentation |
|-----------|--------------------------------|
| `email` present | **Email** (mailto link + validation on save) |
| else `hyperlink` present | **Hyperlink** (URL input / tag list of URLs) |
| else `tags` present | **Tags** (`TagsInput` in edit; compact read-only when not empty) |
| else | Generic string / enum controls |

**`email` wins over `hyperlink`**, and **`hyperlink` wins over `tags`**, when multiple are present on the same string-targeting field.

Object / array-of-object **hyperlink** rows (below) use a separate code path: the field must include the **`hyperlink`** tag, but **`email`** is not defined for OBJECT shapes in the UI.

---

## Known stereotypes

| Tag | Value schema shapes | Model view — read | Model view — edit | Notes |
|-----|---------------------|-------------------|-------------------|-------|
| **`email`** | `STRING`, `ARRAY` of `STRING` | `mailto:` link + envelope icon | `type="email"` input; non-empty values validated with a pragmatic pattern on save | String precedence: wins over `hyperlink` and `tags`. |
| **`hyperlink`** | `STRING` | Clickable external link; bare string = URL as label and target | URL `TextInput` | `https://` prepended when scheme omitted (display/safety helpers in `facetHyperlinkHref`). |
| **`hyperlink`** | `ARRAY` of `STRING` | One link per non-empty string | `TagsInput` for multiple URLs | Same href rules as scalar string. |
| **`hyperlink`** | `OBJECT` | Structured block: **Title** line + **URL** line with link | Generic object form (`title` / `href` fields…) | Expects `href` required for validation; optional `title`. |
| **`hyperlink`** | `ARRAY` of `OBJECT` | Bullet list; each item rendered like OBJECT hyperlink | Array-of-object cards with nested fields | Stereotype is on the **array field**; items are `{ title?, href }` style objects in platform examples. |
| **`tags`** | `ARRAY` of `STRING` | Badge-style or tag list readout | `TagsInput` (comma / Enter) | Active only if `tags` present **and** neither `email` nor `hyperlink` wins on that field. |

Any other tag (e.g. historical **`table`**) is **not** interpreted here; the UI uses default controls from the payload schema.

---

## Validation helpers

- **Email:** `appendEmailStereotypeValidationErrors` — non-empty strings must match the facet email pattern.  
- **Hyperlink objects:** `appendHyperlinkStereotypeValidationErrors` — missing `href` surfaces as a “wrong link” style error path.

---

## Maintenance

When adding a new recognised tag:

1. Extend `facetStereotype.ts` (and tests in `facetStereotype.test.ts`).  
2. Wire `EntityDetails` read/edit as needed.  
3. Update **this file**, [`metadata-canonical-yaml-spec.md`](metadata-canonical-yaml-spec.md) if examples change, and [public facet stereotypes](../../public/src/metadata/facet-stereotypes.md) for operators.

---

## Commit range (reference)

Branch `feat/metadata-rework-final` vs `origin/dev` delivered facet MULTIPLE UX, stereotype handling, admin filters, and related docs; use `git log origin/dev..HEAD` for the exact list.
