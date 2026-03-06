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
            from("edition1")
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

## Edition Inheritance

An edition can inherit feature sets from another edition and add more features:

```kotlin
edition("minimal") {
    features("A", "B", "C")
}

edition("next1") {
    from("minimal")
    feature("D")
}

edition("next2") {
    from("next1")
    feature("E")
}
```

Supported aliases in the block DSL:

- `from("baseEdition")`
- `inherits("baseEdition")`
- `imports("baseEdition")`

Validation rules:

- Unknown inherited edition fails fast.
- Inheritance cycles fail fast.
- Final resolved feature set is inherited features plus local additions.

## End-to-End Chain Example

```kotlin
mill {
    editions {
        defaultEdition = "next2"

        feature("A")
        feature("B")
        feature("C")
        feature("D")
        feature("E")

        edition("minimal") {
            description = "Baseline feature set"
            features("A", "B", "C")
        }

        edition("next1") {
            description = "Minimal + D"
            from("minimal")
            feature("D")
        }

        edition("next2") {
            description = "Next1 + E"
            from("next1")
            feature("E")
        }
    }
}
```

Effective features:

- `minimal` -> `A, B, C`
- `next1` -> `A, B, C, D`
- `next2` -> `A, B, C, D, E`

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

`installBootDist` output is routed to:

- `build/install/<project-name>-boot-<edition>`

`installDist` output is routed to:

- `build/install/<project-name>-<edition>`

`distZip` and `distTar` archive base names are edition-qualified:

- `<project-name>-<edition>.zip`
- `<project-name>-<edition>.tar`

Install tasks evaluate edition content directories by inheritance lineage (base -> ... -> selected edition):

- `src/main/editions/<base-edition>/`
- `...`
- `src/main/editions/<selected-edition>/`

When a directory exists, its content is copied into the root of the edition install output directory.
Copy order follows lineage, so selected edition files can override inherited base edition files.
This applies to both `installBootDist` and `installDist`.

During task execution, Gradle lifecycle logs show each lineage directory as synced or skipped (missing).

Override example for `next2` inheriting from `next1`:

- `src/main/editions/next1/config/application.yml` (base defaults)
- `src/main/editions/next2/config/application.yml` (override)

Because sync order is base-first and selected-edition-last, `next2/config/application.yml` wins when both files exist.

Example:

- `build/install/mill-service-boot-edition1`
- `build/install/mill-service-boot-edition2`
- `build/install/mill-service-edition1`
- `build/install/mill-service-edition2`

## Inspection Tasks

Use this task to inspect configured features and editions:

```bash
./gradlew :apps:mill-service:millListEditions -Pedition=edition2
```

Use this task to print the edition matrix (effective features per edition):

```bash
./gradlew :apps:mill-service:millEditionMatrix
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
