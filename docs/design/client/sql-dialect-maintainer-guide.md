# SQL Dialect Maintainer Guide

Technical runbook for maintainers adding a new SQL dialect to the v2 dialect stack.

This guide assumes the current source of truth is `core/mill-sql` and downstream clients
consume dialect metadata through the typed model and transport contracts.

## 1. Goal and Scope

Use this process when adding a new dialect (for example `SNOWFLAKE`, `BIGQUERY`, `TRINO`) so
all consumers stay aligned:

- Kotlin typed runtime (`core/mill-sql`)
- Server transport (`GetDialect` over gRPC/HTTP)
- Python client (`clients/mill-py`)
- JDBC metadata surface (`clients/mill-jdbc-driver`)
- Documentation and work tracking

## 2. Pre-flight Checklist

Before authoring:

- Confirm dialect id/name and ownership.
- Collect vendor references (SQL grammar, functions, types, metadata docs).
- Decide initial support level:
  - representability-only (conservative defaults where unknown), or
  - validated values (confirmed through tests).

## 3. Add the Dialect YAML

Create a new folder and file under:

- `core/mill-sql/src/main/resources/sql/dialects/<dialect-lower>/<dialect-lower>.yml`

Rules:

- Keep top-level keys aligned with `docs/design/client/sql-dialect-yaml-schema.md`.
- Use kebab-case YAML keys.
- Keep required sections present, even when values are conservative.
- For unknown capability values:
  - use conservative `false` for most feature flags,
  - use nullable tri-state only where supported (`feature-flags.div-is-floordiv`),
  - use `0` for unknown/unlimited numeric limits.

## 4. Validate Kotlin Loading and Registry

The new YAML must deserialize and validate through:

- `DialectLoader`
- `DialectValidator`
- `DialectRegistry`

Typical checks:

- No unknown keys (strict model).
- Required categories exist for `functions` and `operators`.
- `id` matches expected logical id and registry key.

If schema shape changed, update:

- `core/mill-sql/.../SqlDialectSpec.kt`
- validator rules in `core/mill-sql/.../DialectValidator.kt`

Do not patch generated artifacts manually.

## 5. Verify Transport Contract Mapping

Ensure `SqlDialectSpec -> DialectDescriptor` mapping includes new/changed fields:

- mapper(s) in data/service modules (`DialectProtoMapper` and related wiring)
- `GetDialect` service path returns the new dialect by id
- handshake capability remains correct (`supportDialect`)

If contract fields changed, update proto and regenerate clients where required.

## 6. Client Integration Checks

### Python (`clients/mill-py`)

- `MillClient.get_dialect()` and async equivalent return the new descriptor.
- gRPC and HTTP transports both deserialize the dialect correctly.
- Add/extend tests for:
  - explicit dialect id fetch,
  - fallback behavior (only for legacy servers, if still supported),
  - key feature flag/function/type-info presence.

### JDBC (`clients/mill-jdbc-driver`)

- `MillDatabaseMetadata` reflects the new dialect values (limits, transactions, type-info, capabilities).
- `getTypeInfo()` rows map correctly from `type-info`.
- Add/adjust metadata assertions in unit tests.

## 7. Validation Strategy

Minimum bar for new dialect onboarding:

1. YAML parse/validation test.
2. `GetDialect` retrieval test.
3. One downstream consumer test (Python or JDBC) asserting mapped behavior.

Recommended hardening:

- Run/extend dialect correctness validation (`WI-023` scope) for function/operator claims.
- Add at least one negative assertion (unsupported feature remains unsupported).

## 8. Documentation Updates (Required)

When adding a dialect, update:

- `docs/design/client/sql-dialect-yaml-schema.md` (if schema/rules changed)
- `docs/workitems/BACKLOG.md` (new or changed tracked items)
- `docs/workitems/MILESTONE.md` (status and placement)

If no schema changes were made, still mention the new dialect coverage in the related WI or PR.

## 9. Common Pitfalls

- Adding YAML keys not present in typed Kotlin model.
- Updating YAML without matching validator/category constraints.
- gRPC path works but HTTP path missing parity.
- Assuming feature support without correctness validation (especially ibis-sensitive features).
- Forgetting to update metadata/type-info consumers after YAML additions.

## 10. Completion Checklist

- [ ] New dialect YAML added under `core/mill-sql/src/main/resources/sql/dialects/`.
- [ ] Kotlin loader/validator/registry pass.
- [ ] `GetDialect` returns expected descriptor for the new id.
- [ ] Python transport/client tests updated and passing.
- [ ] JDBC metadata mapping tested (if affected).
- [ ] Backlog and milestone docs updated.
- [ ] Design docs updated (schema/guide references) when needed.
