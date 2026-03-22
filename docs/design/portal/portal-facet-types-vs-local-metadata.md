# Portal — central facet types vs local instance metadata

**Status:** Directional (aligns with federated portal vision)  
**Last updated:** 2026-03-22  
**Related:** `portal-federated-metadata-landscape.md`, `docs/design/metadata/dynamic-facet-types-schema-and-validation.md`

---

## Separation of concerns

The platform distinguishes two layers that must not be conflated:

| Layer | What it is | Where it lives |
|-------|------------|----------------|
| **Facet types (definitions)** | Canonical **vocabulary and contracts**: facet-type URNs, payload structure (`FacetPayloadSchema` / v3-style schema tree), `applicableTo` entity-type URNs, mandatory/display flags, versioning for validation and UI. | **Portal (central)** — maintained as the org-wide source of truth, typically delivered from a **central portal repo** or portal service that publishes releases. |
| **Metadata (state)** | **Values** attached to entities: facet payloads, scopes, entity ids, audit history, imports. | **Local to each Mill instance** — operational source of truth for that deployment’s data assets. |

**Rule of thumb:** the portal answers *“what kinds of facets and shapes are valid in this organization?”* Each instance answers *“what metadata do we actually have on this schema/table/column?”*

---

## Facet types — portal-sourced

- **Central registry** — New facet types, schema changes, and deprecations are introduced **through the portal pipeline** (or the git repo the portal treats as canonical), not by ad-hoc edits on every instance.
- **Consumption** — Instances **subscribe** to released **facet type bundles** (versioned): e.g. sync at deploy, periodic pull, or admin-triggered import into local `FacetCatalog` / `metadata_facet_type` seeds.
- **Benefits** — One shared semantic vocabulary across instances; consistent **JSON Schema generation per entity type** (see metadata design note); LLM and UI contracts stay aligned org-wide.

Implementation details (transport, signing, rollback) are TBD; the **contract** is: **definitions are not owned independently per instance**.

---

## Metadata — instance-local

- **Persistence** — `MetadataEntity`, scoped facet payloads, promotion records, and operation audit remain **on the instance** (file-backed or JPA per instance config).
- **Portal role** — The portal **aggregates and displays** metadata published from instances for landscape and collaboration; it does **not** replace local storage as the operational system of record unless a future product explicitly adds central replication (out of scope here).
- **Federation** — Cross-instance views combine **local state** with **shared facet type semantics** so comparisons and scoring use the same labels and validation rules.

---

## Interaction sketch

1. Portal team publishes **facet type bundle** `v1.4.0` (URNs + payload schemas + applicable entity types).
2. Mill instance applies bundle → local catalog updated; existing **metadata values** unchanged unless a migration is defined.
3. Users edit metadata locally; validation uses **portal-defined** payload schemas.
4. Portal ingests **metadata snapshots or APIs** from instances for federated UI — **values** are read from sources, **types** are interpreted using the central registry the portal also ships.

---

## Relationship to “shared semantic model”

This note refines the **shared semantic model** bullet in `portal-federated-metadata-landscape.md`: that model is **facet type definitions and contracts** (portal/central), distinct from **source metadata** (instance-local state).

---

## References

- `docs/design/portal/portal-federated-metadata-landscape.md` — federated landscape, collaboration, registry questions.
- `docs/design/metadata/dynamic-facet-types-schema-and-validation.md` — `FacetPayloadSchema`, validation, JSON Schema per entity type.
