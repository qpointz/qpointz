# WI-014 — Gradle Multi-Edition BootDist Plugin (Round 1)

Status: `planned`  
Type: `✨ feature`  
Area: `platform`, `build-logic`, `apps`  
Backlog refs: `P-32`

## Problem Statement

`apps/mill-service` needs controlled multi-edition packaging while staying a single
Spring Boot application module with one `main` class and one application name.
Current setup does not provide an edition model for `bootDist` and `installBootDist`
that isolates outputs per edition.

## Goal

Create a reusable Gradle convention plugin in `build-logic` that enables
predefined edition builds for Spring Boot distributions, without introducing
multiple app modules, `shadowJar`, or arbitrary feature combinations.

Round 1 is build-only scaffolding: feature/module mapping and edition sets remain
declarative and easy to edit in the app module.

## In Scope

1. Add a dedicated plugin in `build-logic` (for example `io.qpointz.plugins.mill-editions`).
2. Provide an extension model for:
   - feature-to-project-module mapping
   - edition-to-feature-set mapping
   - default edition
3. Resolve selected edition via project property (`-Pedition=<name>`) with strict allowed values.
4. Wire selected-edition dependencies into the app classpath at configuration time.
5. Configure `bootDist`/`installBootDist` outputs to edition-specific install directories.
6. Register edition value as task input to preserve incremental/cached correctness.
7. Apply plugin in `apps/mill-service` and keep module build script mostly declarative.

## Out of Scope

- Runtime feature flags/conditions redesign.
- Dynamic validation of arbitrary feature combinations beyond declared editions.
- Additional application modules or repackaging approach changes.
- `shadowJar`-based edition artifacts.

## First-Round Edition Shape

Initial example edition declarations (editable by owner):

- `edition1`: `metadata`
- `edition2`: `metadata`, `aiv1`

The plugin must stay generic so names and module mappings can be changed without
rewriting plugin mechanics.

## Implementation Plan

1. **Plugin registration in build-logic**
   - Register new plugin id and implementation class in `build-logic/build.gradle.kts`.
2. **Plugin implementation**
   - Add extension data model and edition resolver in `build-logic/src/main/kotlin/io/qpointz/mill/plugins/`.
   - Fail fast for unknown/undefined edition values.
3. **Edition dependency wiring**
   - Translate selected edition features into configured project dependencies.
4. **Distribution configuration**
   - Keep canonical `bootDist`/`installBootDist` tasks.
   - Route install output to edition-qualified paths under `build/install/`.
5. **App module adoption**
   - Apply plugin in `apps/mill-service/build.gradle.kts`.
   - Define only feature/module map and edition map there.

## Acceptance Criteria

- `apps/mill-service` remains one Spring Boot module with unchanged main class and app name.
- Only predefined editions are selectable; unknown edition fails early.
- `installBootDist` supports `-Pedition=<name>` and writes each edition to a separate directory.
- Re-running the same edition with no source changes avoids unnecessary recompilation.
- No `shadowJar` and no additional app modules are introduced.

## Test Plan (during implementation)

- Run:
  - `:apps:mill-service:installBootDist -Pedition=edition1`
  - `:apps:mill-service:installBootDist -Pedition=edition2`
- Verify:
  - distinct install output directories
  - stable script/application naming
  - expected classpath/module differences by edition
  - incremental behavior on repeated same-edition runs

## Deliverables

- This work item definition (`docs/workitems/WI-014-gradle-multi-edition-bootdist-plugin.md`).
- Backlog linkage (`P-32` in `docs/workitems/BACKLOG.md`).
