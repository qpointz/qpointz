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
