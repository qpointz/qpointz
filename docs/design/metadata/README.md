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
| `metadata-documentation.md` | User-facing docs: concepts, browser, facets, configuration, practices |
| `metadata-implementation-roadmap.md` | Roadmap for faceted metadata system, multi-file repo, ValueMappingFacet |
| `metadata-provider-refactoring-plan.md` | Plan to replace legacy MetadataProvider with facet-based system |
| `metadata-service-design.md` | Faceted metadata service design: hierarchy, phases, tree API, URL routing; updated March 2026 with persistence/scope delivery status |
| `metadata-ui-implementation-plan.md` | Metadata UI phases: core, REST, browser, editing |
| `value-mapping-tactical-solution.md` | Tactical YAML-based value-mapping config (FileRepository) |
| `value-mapping-via-metadata-provider.md` | Value mapping via MetadataProvider (facet-based approach) |
