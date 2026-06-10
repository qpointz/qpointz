# Metadata Subsystem

Design documents for the Mill metadata service, providers, and related UI.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Metadata service architecture (faceted design, tree API, URL routing)
- Metadata provider design and refactoring (legacy MetadataProvider replacement)
- Value mappings (tactical YAML-based solutions, future facet-based mappings)
- Metadata UI implementation (browser, editing, REST integration)
- Metadata implementation roadmaps and phase tracking
- Metadata user-facing documentation and concepts

## Does NOT Belong Here

- General UI/UX patterns not specific to metadata → `ui/`
- Data type definitions consumed by metadata → `data/`
- AI features that query metadata for context → `ai/`

## Documents

| File | Description |
|------|-------------|
| `collaborative-metadata-requirements.md` | Collaborative editing requirements: scopes, permissions, audit |
| `dynamic-facet-types-schema-and-validation.md` | Open design: typed vs dynamic facet types; structure, validation, serialization; alignment with ai/v3 `ToolSchemaYaml` / capability manifests |
| `facet-type-descriptor-formats.md` | Canonical facet type descriptor format: strict JSON contract, YAML equivalent examples, URN normalization and ordering rules |
| `facet-payload-schema-reference.md` | **`contentSchema`** payload tree: type vocabulary, array-of-objects example (`ai-column-value-mapping-values`), links to seeds |
| `platform-standard-facet-types.md` | **Inventory:** all 17 platform seed facet types (`platform-bootstrap.yaml` + `platform-flow-facet-types.yaml`), URN slug, purpose, applicability |
| `metadata-documentation.md` | User-facing docs: concepts, browser, facets, configuration, practices |
| `mill-metadata-domain-model.md` | **Reference:** entities, facet types, assignments (`FacetInstance`), scopes, `MetadataReadContext`, merge / `merge_action` — aligns with workitem SPEC / greenfield implementation |
| `mill-ui-facet-stereotypes.md` | **Reference:** known `stereotype` tags recognised by `mill-ui` (hyperlink, email, tags), precedence, wire shapes; implementation pointers |
| `metadata-layered-sources-and-ephemeral-facets.md` | **Story / backlog M-31:** readonly `MetadataSource` aggregation (repository + inferred subsystem sources), inferred facets (e.g. authorization), merged schema snapshot, UI constellation with per-instance `origin` / `originId` and captured-only edit |
| `backend-provided-metadata.md` | Backend-specific **inferred** facets (e.g. flow descriptors): purpose vs logical layout, `MetadataOriginIds.FLOW`, `mill.data.backend.flow.metadata` / `cache.facets`, flow-only autoconfigure placement |
| `metadata-facet-type-catalog-defined-and-observed.md` | **Backlog M-32:** facet type admin lists **DEFINED** + **OBSERVED** runtime types (`FacetTypeSource`); API list merge + UI source badges/filters |
| `metadata-implementation-roadmap.md` | Roadmap for faceted metadata system, multi-file repo, ValueMappingFacet |
| `metadata-provider-refactoring-plan.md` | Plan to replace legacy MetadataProvider with facet-based system |
| `metadata-synthetic-to-canonical-writer-handoff.md` | **Handoff:** synthetic `schemas:` dataset YAML → repository `entities` → canonical envelope / §15.2 multi-document writer (field mappings, URNs, relations, scripts) |
| `metadata-service-design.md` | Service architecture + implementation notes, including manifest-based facet type API, registry strategy contract, and startup import behavior |
| `metadata-ui-implementation-plan.md` | UI planning + implementation notes for admin facet type management (routes, flags, split editor, expert JSON mode) |
| `model-view-facet-boxes.md` | Data Model explorer (`/model`): standard vs custom facet boxes, MULTIPLE-instance layout, extension points |
| `facet-class-elimination.md` | **Proposed:** remove "blessed" concrete facet classes (`DescriptiveFacet`, `ConceptFacet`, `ValueMappingFacet`, etc.) that duplicate the generic `FacetInstance` + `FacetPayloadSchema` architecture; demote to plain data containers; dead-code inventory |
| `value-mapping-tactical-solution.md` | Tactical YAML-based value-mapping config (FileRepository) |
| `value-mapping-via-metadata-provider.md` | Value mapping via MetadataProvider (facet-based approach) |

**Implementing a backend `MetadataSource`:** [`../data/implementing-backend-metadata-source.md`](../data/implementing-backend-metadata-source.md) (shared foundation vs per-backend facet URNs, `category`, `originId`).
