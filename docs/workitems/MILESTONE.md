# Milestones

## 0.7.0

**Target date:** TBD

### Completed

Items delivered in this milestone.

- WI-015 — Core `mill-sql` bootstrap + feature-complete YAML schema
- WI-016 — Migrate `POSTGRES`/`H2`/`CALCITE`/`MYSQL` to new schema
- WI-017 — Kotlin typed dialect model + YAML loader (`core/mill-sql` only)
- WI-018 — `GetDialect` contracts for gRPC/HTTP + handshake support flag
- WI-019 — Server `GetDialect` implementation backed by migrated dialects
- WI-020 — Migrate AI dialect consumer to new typed runtime model
- WI-022 — Fully document SQL dialect schema in design docs
- WI-026 — JDBC full metadata implementation (`DatabaseMetaData` surface)
- WI-021 — Python remote dialect consumption over gRPC/HTTP
- WI-024 — Python SQLAlchemy implementation (MillDialect + compiler + entry points)
- WI-025 — Python ibis initial implementation (BaseBackend + SQL compilation, slice 1)

Completed WI markdown files are intentionally removed after delivery; this milestone list is the
retained canonical record of completed items.

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items targeted next after in-progress work is completed.

- WI-023 — ibis dialect correctness validation and certification
  (`docs/workitems/WI-023-ibis-dialect-correctness-validation.md`)

## 0.8.0

**Target date:** TBD

### Completed

Items delivered in this milestone.

- WI-062 — Schema data aggregation boundary: `SchemaFacetService`, `*WithFacets` domain model,
  `SchemaFacets` (map-backed, typed facet holder), `SchemaFacetResult` with unbound metadata,
  `SchemaFacetAutoConfiguration`; unit + skymill integration tests in `data/mill-data-schema-core`

### In Progress

Items currently being implemented in this milestone.

No active in-progress items currently.

### Planned

Items targeted next after 0.7.0 closure and backlog triage.

- WI-027 — Metadata value mapping bridge and parity
  (`docs/workitems/WI-027-metadata-value-mapping-bridge.md`)
- WI-028 — Metadata value mapping API and UI surface
  (`docs/workitems/WI-028-metadata-value-mapping-api-and-ui.md`)
- WI-029 — Metadata relational persistence and repository transition
  (`docs/workitems/WI-029-metadata-relational-persistence.md`)
- WI-030 — Metadata user editing and authoring workflow
  (`docs/workitems/WI-030-metadata-user-editing.md`)
- WI-031 — Metadata scopes and context composition
  (`docs/workitems/WI-031-metadata-scopes-and-contexts.md`)
- WI-032 — Metadata context promotion workflow
  (`docs/workitems/WI-032-metadata-promotion-workflow.md`)
- WI-033 — Metadata service API cleanup and error handling
  (`docs/workitems/WI-033-metadata-service-cleanup.md`)
- WI-034 — Metadata complex type support in structural facets and UI
  (`docs/workitems/WI-034-metadata-complex-type-support.md`)
