# Facet UI customization (future work)

## Context

The Data Model explorer (`ui/mill-ui`, `/model`) renders metadata facets primarily from **facet type manifests** (`FacetTypeManifest` / payload schema) and shared helpers in `EntityDetails.tsx` (descriptor-driven read and edit, including `MULTIPLE` cardinality as one card per instance).

Historically, the codebase also carried **facet-specific presentational components** (for example, bespoke read views for descriptive and relation payloads). That approach is valid: a platform may offer a **customized view** per facet type URN for clearer UX than generic schema rendering.

Those bespoke components were **removed** from the production tree once the standard descriptor path covered the same scenarios; the idea remains sound and is worth revisiting when priorities allow.

## Direction (not implemented)

Customization should be **redesigned** rather than reinstated ad hoc:

1. **Split contracts** — Register optional **view** (read-only) and **edit** components per facet type (or per URN suffix), instead of a single mixed component.
2. **Stable props** — Components should receive a small, explicit contract (for example: entity context, resolved payload, manifest/descriptor, mode, and save/delete callbacks) so facet-specific UX stays testable and swappable.
3. **Fallback** — When no custom view/edit is registered, use today’s descriptor-driven renderer and expert JSON path.
4. **Platform vs extension** — Distinguish built-in registrations (structural, relation, descriptive) from third-party or optional bundles.

This is **not** top priority; track follow-up in [`docs/workitems/BACKLOG.md`](../../workitems/BACKLOG.md) (see **U-12**).

## Related

- Model view behavior and API shape: [`docs/design/metadata/model-view-facet-boxes.md`](../metadata/model-view-facet-boxes.md)
- Facet type descriptor formats: [`docs/design/metadata/facet-type-descriptor-formats.md`](../metadata/facet-type-descriptor-formats.md)
