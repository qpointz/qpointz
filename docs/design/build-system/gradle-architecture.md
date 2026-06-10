# Gradle Architecture

## Overview

The repository uses one root Gradle build with many included modules. Build structure is primarily defined in:

- `settings.gradle.kts`
- `build.gradle.kts`
- `libs.versions.toml`
- `build-logic/`

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

- `io.qpointz.plugins.mill`
- `io.qpointz.plugins.mill-aggregate`
- `io.qpointz.plugins.mill-publish`

Detailed plugin behavior, extension model, and edition internals are documented in `gradle-plugins.md`.

## Versioning and Dependency Management

- Dependency versions and plugin aliases are centralized in `libs.versions.toml`.
- Modules should consume catalog aliases instead of hard-coded versions where possible.
- Root wrapper version is defined in `gradle/wrapper/gradle-wrapper.properties` (currently Gradle `9.3.1`).

## Root Aggregation Tasks

`build.gradle.kts` registers root-level aggregate tasks:

- `test`
- `testITClasses`
- `testIT`
- `publishSonatypeBundle`

It also configures Dokka aggregation over top-level module groups.

## Practical Rules

- Use repository-root `./gradlew` for cross-module operations.
- Keep version definitions in `libs.versions.toml`.
- Prefer convention plugins over per-module copy/paste Gradle logic.
- Update both `settings.gradle.kts` and root aggregate tasks when module groups change.
- **`@ConfigurationProperties` in autoconfigure modules**: add
  `annotationProcessor(libs.spring.boot.configuration.processor)` (Java) or provide
  `META-INF/additional-spring-configuration-metadata.json` (Kotlin) so IDE metadata is generated.
  See `docs/design/platform/mill-configuration.md` for the full rule.

For day-to-day command recipes, see `maintainer-recipes.md`.
