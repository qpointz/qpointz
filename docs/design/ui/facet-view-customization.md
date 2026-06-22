# Facet UI customization (future work)

## Context

The Data Model explorer (`ui/mill-ui`, `/model`) and **general chat facet captures** share a
**read-only facet module** at `ui/mill-ui/src/components/data-model/facets/` (`FacetReadOnlyBody`,
`FacetPayloadReadOnly`, `facetDisplayUtils`). Data Model hosts it from `EntityDetails.tsx` (category
tabs, edit/delete, `MULTIPLE` nesting); chat hosts it from `FacetCondensedPreview.tsx` (artefact
shell + normalisation). Edit mode and entity chrome remain Data Model–only.

Descriptor-driven rendering uses **facet type manifests** (`FacetTypeManifest` / payload schema).

Historically, the codebase also carried **facet-specific presentational components** (for example, bespoke read views for descriptive and relation payloads). That approach is valid: a platform may offer a **customized view** per facet type URN for clearer UX than generic schema rendering.

Those bespoke components were **removed** from the production tree once the standard descriptor path covered the same scenarios; the idea remains sound and is worth revisiting when priorities allow.

## Direction (not implemented)

Customization should be **redesigned** rather than reinstated ad hoc:

1. **Split contracts** — Register optional **view** (read-only) and **edit** components per facet type (or per URN suffix), instead of a single mixed component.
2. **Stable props** — Components should receive a small, explicit contract (for example: entity context, resolved payload, manifest/descriptor, mode, and save/delete callbacks) so facet-specific UX stays testable and swappable.
3. **Fallback** — When no custom view/edit is registered, use today’s shared descriptor-driven renderer (`FacetReadOnlyBody` / edit path in `EntityDetails`) and expert JSON path.
4. **Platform vs extension** — Distinguish built-in registrations (structural, relation, descriptive) from third-party or optional bundles.

This is **not** top priority; track follow-up in [`docs/workitems/BACKLOG.md`](../../workitems/BACKLOG.md) (see **U-12**).

## Related

- Model view behavior and API shape: [`docs/design/metadata/model-view-facet-boxes.md`](../metadata/model-view-facet-boxes.md)
- Chat facet shell + replay: [`docs/design/ai/chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md) §7.1
- Facet type descriptor formats: [`docs/design/metadata/facet-type-descriptor-formats.md`](../metadata/facet-type-descriptor-formats.md)
