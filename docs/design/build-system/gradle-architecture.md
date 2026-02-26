# Gradle Architecture

## Overview

The repository uses a single root Gradle build with many included projects. Core module inclusion and plugin wiring are defined in:

- `settings.gradle.kts`
- `build.gradle.kts`
- `libs.versions.toml`
- `build-logic/src/main/kotlin/io/qpointz/mill/plugins/`

## Multi-Module Topology

`settings.gradle.kts` defines `rootProject.name = "mill"` and includes grouped modules such as:

- `:core:*`
- `:data:*`
- `:metadata:*`
- `:ai:*`
- `:clients:*`
- `:apps:*`
- `:ui:*`

This provides one dependency graph and one task namespace at repository root.

## Build Conventions (build-logic)

`pluginManagement { includeBuild("build-logic") }` in `settings.gradle.kts` registers local convention plugins:

- `MillPlugin`
  - Sets group/version defaults.
  - Enforces Java toolchain 21.
  - Applies `java`, `jacoco`, `jvm-test-suite`.
  - Adds project to root jacoco aggregation.
- `MillAggregatePlugin`
  - Adds aggregate lifecycle tasks (`test`, `compileTestIT`, `testIT`, jacoco tasks) over subprojects.
- `MillPublishPlugin`
  - Configures `maven-publish` + `signing`.
  - Produces sources/javadoc artifacts.
  - Publishes to root `build/repo`.

## Versioning and Dependency Management

- Dependency versions and plugin aliases are centralized in `libs.versions.toml`.
- Modules should consume catalog aliases instead of hard-coded versions where possible.
- Root wrapper version is defined in `gradle/wrapper/gradle-wrapper.properties` (currently Gradle `9.3.1`).

## Root Aggregation Tasks

`build.gradle.kts` registers root-level aggregate tasks:

- `test`
- `compileTestIT`
- `testIT`
- `publishSonatypeBundle`

It also configures Dokka aggregation over top-level module groups.

## Maintainer Notes

- Use repository-root `./gradlew` for cross-module operations.
- Keep module-specific CI commands rooted at repository root paths to avoid task path drift.
- When adding/removing module groups, update both `settings.gradle.kts` includes and root aggregate task lists in `build.gradle.kts`.
