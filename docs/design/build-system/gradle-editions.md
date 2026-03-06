# Gradle Editions for `mill` Plugin

This document describes the edition model introduced in the `io.qpointz.plugins.mill` convention plugin for application modules such as `apps/mill-service`.

## Why Editions

Editions allow one Spring Boot app module to produce multiple distribution variants without creating separate app modules. Each edition activates a named feature set. Features can control:

- Dependency inclusion (module wiring).
- Build-script conditions (`isActive("...")` checks).
- Task behavior keyed by selected edition.

## DSL Overview

Editions are configured inside the existing `mill {}` block:

```kotlin
mill {
    description = "calcite service desc"
    publishArtifacts = false

    editions {
        defaultEdition = "edition1"

        feature("metadata") {
            description = "Core metadata capabilities"
        }

        feature("aiv1") {
            description = "AI v1 NL-SQL chat support"
            module(":ai:mill-ai-v1-nlsql-chat-service")
            module(":ai:mill-ai-v1-core")
        }

        edition("edition1") {
            description = "Base metadata-only edition"
            feature("metadata")
        }

        edition("edition2") {
            description = "Metadata + AI v1 edition"
            features("metadata", "aiv1")
        }
    }
}
```

Short-form variants are also supported:

```kotlin
feature("aiv1", ":ai:mill-ai-v1-nlsql-chat-service", ":ai:mill-ai-v1-core")
edition("edition2", "metadata", "aiv1")
```

## Feature Semantics

- A feature can have zero modules (`feature("metadata")`).
- Modules are optional and additive per feature.
- Feature descriptions are metadata for documentation/listing tasks.

If a selected edition references an undefined feature, the build fails fast.

## Edition Selection

Edition resolution order:

1. `-Pedition=<name>`
2. `mill.editions.defaultEdition`

If no edition can be resolved, the build fails fast.
If an unknown edition is passed, the build fails fast with allowed values.

## Dependency Wiring

For the selected edition, all modules from active features are added to the module's `implementation` configuration.

Implications:

- The modules are on compile and runtime classpaths.
- They appear in `runtimeClasspath` dependency reports.

## Runtime Feature Checks in Build Scripts

The DSL exposes providers for conditional build logic:

- `mill.editions.selectedEdition: Provider<String>`
- `mill.editions.activeFeatures: Provider<Set<String>>`
- `mill.editions.isActive("featureName"): Provider<Boolean>`

Example:

```kotlin
if (mill.editions.isActive("aiv1").get()) {
    // configure tasks/resources for AI variant
}
```

## Distribution Behavior

`bootDist` and `installBootDist` remain canonical task names.
The selected edition is registered as task input (`mill.edition`) for cache/incremental correctness.

`installBootDist` output is routed to an edition-qualified location:

- `build/install/<project-name>-<edition>`

Example:

- `build/install/mill-service-edition1`
- `build/install/mill-service-edition2`

## Inspection Tasks

Use this task to inspect configured features and editions:

```bash
./gradlew :apps:mill-service:millListEditions -Pedition=edition2
```

The output includes:

- Default and selected edition.
- Feature list with descriptions and module mappings.
- Edition list with descriptions and active feature sets.

For dependency graph inspection by edition:

```bash
./gradlew :apps:mill-service:dependencies --configuration runtimeClasspath -Pedition=edition1
./gradlew :apps:mill-service:dependencies --configuration runtimeClasspath -Pedition=edition2
```
