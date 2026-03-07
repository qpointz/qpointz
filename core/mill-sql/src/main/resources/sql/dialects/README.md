# Dialect Resources Baseline

These dialect YAML files were seeded from `core/mill-core` as a baseline input for the
`core/mill-sql` migration program.

- `mill-core` remains unchanged.
- Files in this directory are the new working copy for schema migration and typed runtime loading.
- Current scoped dialect set in `core/mill-sql`:
  - `calcite`
  - `postgres`
  - `h2`
  - `mysql`
- Accuracy/validation and full-schema migration are handled in follow-up work items.
