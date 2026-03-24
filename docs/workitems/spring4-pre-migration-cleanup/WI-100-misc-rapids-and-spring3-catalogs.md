# WI-100 — `misc/` non-product trees: catalogs and stale paths

Status: `planned`  
Type: `refactoring`  
Area: `build`, `docs`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

The migration plan references **`misc/rapids/`** (version catalog and `javax.servlet` usage). That
tree may be absent, moved, or never included in the root Gradle build — leaving **stale design doc
paths** and unclear ownership.

## Goal

- Either **repair** standalone Gradle metadata under `misc/` so developers can build those trees when
  needed, or **document removal** and update design references so the platform plan matches reality.
- Avoid implying `misc/` code blocks the **product** Boot 4 migration when it is not on the CI graph.

## Scope

1. Inventory `misc/` for: `rapids*`, `spring-3`, and any other Gradle projects with their own version
   catalog or broken `libs.versions.*` references.
2. For each tree: decide **maintained** (fix catalog / imports) vs **archive-only** (no build
   guarantee).
3. Update **`docs/design/platform/spring4-migration-plan.md`** module matrix entries if paths or
   risk classification change (or defer doc update to **WI-104** if you prefer a single doc PR).

## Acceptance Criteria

- No known broken `libs.versions.*` reference inside **intentionally maintained** `misc/` Gradle
  projects, or they are marked archived in README.
- Design docs do not claim files exist at paths that were removed.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../design/platform/spring4-migration-plan.md) — §11, module matrix `misc/rapids`
