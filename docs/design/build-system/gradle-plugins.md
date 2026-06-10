# Mill Gradle Plugins (`build-logic`)

This document is the single reference for all `Mill*` plugins and extension components implemented in `build-logic`.

## Plugin Catalog

Defined in `build-logic/build.gradle.kts`:

- `io.qpointz.plugins.mill` -> `MillPlugin`
- `io.qpointz.plugins.mill-publish` -> `MillPublishPlugin`
- `io.qpointz.plugins.mill-aggregate` -> `MillAggregatePlugin`

---

## `MillPlugin` (`io.qpointz.plugins.mill`)

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillPlugin.kt`

Primary project convention plugin for module builds.

### What it configures

- Creates/uses `mill {}` extension (`MillExtension`).
- Registers edition inspection tasks:
  - `millListEditions`
  - `millEditionMatrix`
- Sets project coordinates:
  - `group = "io.qpointz.mill"`
  - `version` from:
    1) `-PprojectVersion`, otherwise
    2) `../VERSION`, otherwise
    3) fallback `0.1.0`
- Applies base plugins:
  - `java`
  - `jacoco`
  - `jvm-test-suite`
- Enforces Java 21 toolchain (and Kotlin JVM toolchain 21 when Kotlin plugin is present).
- Sets `Test.workingDir` to module directory.
- Enables JaCoCo XML report when `jacocoTestReport` exists.
- Adds project to root `jacocoAggregation` dependency bucket.
- Extends `clean` to remove module `bin` directory.
- If `mill.editions` is configured, wires edition-aware packaging behavior.
- If `mill.publishArtifacts == true`, applies `MillPublishPlugin`.

### Typical usage

```kotlin
plugins {
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Module description used in publishing metadata"
    publishArtifacts = false
}
```

### DSL reference: `mill { ... }`

Available when `io.qpointz.plugins.mill` is applied.

| Field / function | Type | Default | Purpose |
|---|---|---|---|
| `description` | `String` | `""` | Used by publishing metadata (`MillPublishPlugin`) |
| `publishArtifacts` | `Boolean` | `true` | If `true`, applies publishing conventions |
| `publishArtefact` | `Boolean` | mirrors `publishArtifacts` | Legacy alias spelling |
| `editions { ... }` | block | not configured | Configures `MillEditionsExtension` |

---

## `MillPublishPlugin` (`io.qpointz.plugins.mill-publish`)

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillPublishPlugin.kt`

Convention plugin for repository-local publishing and signing.

### What it configures

- Applies:
  - `maven-publish`
  - `signing`
- Enables `withSourcesJar()`.
- Registers `javadocJar`:
  - uses Dokka publication javadoc if available
  - otherwise falls back to `javadoc` task output
- Configures signing for publications (disabled when no signing key is provided).
- Configures local Maven repository target:
  - `rootProject/build/repo`
- Creates `mavenJava` publication after evaluation.
- Applies common POM metadata via `applyMillPomMetadata(...)`.

### POM metadata fields

- name, description
- project URL
- Apache 2.0 license
- developer metadata
- SCM URLs

### DSL/reference notes

- This plugin has no custom extension block.
- It is typically applied automatically by `mill.publishArtifacts = true`.
- Manual apply is still supported:

```kotlin
plugins {
    id("io.qpointz.plugins.mill-publish")
}
```

---

## `MillAggregatePlugin` (`io.qpointz.plugins.mill-aggregate`)

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillAggregatePlugin.kt`

Convention plugin for aggregator modules (projects that mostly delegate to subprojects).

### What it configures

Registers aggregate verification tasks (if missing in current project):

- `test`
- `testITClasses`
- `testIT`
- `jacocoTestReport`
- `jacocoTestCoverageVerification`

Each task depends on same-named tasks discovered in direct subprojects.

### Typical usage

```kotlin
plugins {
    id("io.qpointz.plugins.mill-aggregate")
}
```

### DSL/reference notes

- This plugin has no custom extension block.
- Behavior is task registration only (aggregate verification tasks).

---

## Extension and Edition Components

These are internal `Mill*` components used by `MillPlugin`.

### `MillExtension`

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillExtension.kt`

- `description: String`
- `publishArtifacts: Boolean` (`publishArtefact` legacy alias is supported)
- `editions: MillEditionsExtension`

DSL shape:

```kotlin
mill {
    description = "..."
    publishArtifacts = true
    editions {
        // edition model
    }
}
```

### `MillEditionsExtension`

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillEditionsExtension.kt`

Provides edition DSL and resolution logic:

- feature declarations (`feature(...)`)
- edition declarations (`edition(...)`)
- inheritance (`from`, `inherits`, `imports`)
- selected edition resolution from `-Pedition` or `defaultEdition`
- effective feature matrix and lineage resolution
- validation:
  - unknown edition
  - unknown inherited edition
  - inheritance cycles
  - undefined features referenced by edition

DSL shape:

```kotlin
mill {
    editions {
        defaultEdition = "integration"

        feature("sample-data") {
            description = "Sample datasets"
            module(":some:module")
            modules(":module:a", ":module:b")
        }

        // Short form (modules only)
        feature("aiv1", ":ai:mill-ai-v1-nlsql-chat-service", ":ai:mill-ai-v1-core")

        edition("minimal") {
            description = "Base"
        }

        edition("integration") {
            description = "Inherited edition"
            from("minimal")        // aliases: inherits("minimal"), imports("minimal")
            feature("sample-data")
            features("sample-data")
        }

        // Short form (features only)
        edition("smoke", "sample-data")
    }
}
```

#### DSL reference: `mill.editions { ... }`

| Field / function | Type | Purpose |
|---|---|---|
| `defaultEdition` | `String?` | Fallback edition when `-Pedition` is not set |
| `feature(name, vararg modules)` | function | Declares feature and module mapping |
| `feature(name) { ... }` | function/block | Declares feature with metadata/modules |
| `edition(name, vararg features)` | function | Declares edition and local features |
| `edition(name) { ... }` | function/block | Declares edition with metadata/inheritance/features |
| `isActive(name)` | `Provider<Boolean>` | Build-script conditional by feature |
| `selectedEdition` | `Provider<String>` | Resolved selected edition |
| `activeFeatures` | `Provider<Set<String>>` | Effective features for selected edition |

#### Nested feature block (`feature("x") { ... }`)

| Field / function | Type | Purpose |
|---|---|---|
| `description` | `String?` | Human-readable feature description |
| `module(path)` | function | Adds one project dependency path |
| `modules(vararg paths)` | function | Adds multiple project dependency paths |

#### Nested edition block (`edition("x") { ... }`)

| Field / function | Type | Purpose |
|---|---|---|
| `description` | `String?` | Human-readable edition description |
| `feature(name)` | function | Adds one local feature |
| `features(vararg names)` | function | Adds multiple local features |
| `from(base)` | function | Inherit another edition |
| `inherits(base)` | function | Alias of `from` |
| `imports(base)` | function | Alias of `from` |

#### Edition resolution order

1. `-Pedition=<name>`
2. `defaultEdition`

Build fails fast on unknown edition, unknown inherited edition, cycle, or undefined referenced feature.

### `MillEditionPackaging`

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillEditionPackaging.kt`

Configures edition-aware packaging tasks:

- output folders:
  - `installBootDist` -> `build/install/<project>-boot-<edition>`
  - `installDist` -> `build/install/<project>-<edition>`
- archive names:
  - `distZip` / `distTar` base name -> `<project>-<edition>`
- module wiring:
  - active-feature modules added to `implementation`
- edition content merge:
  - merges `src/main/editions/<edition>/` in lineage order
  - base editions first, selected edition last (override-friendly)
  - logs sync/skip decisions at lifecycle level

### `MillEditionTasks`

Source: `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillEditionTasks.kt`

Registers helper tasks:

- `millListEditions` -> prints configured features/editions and selected/default edition
- `millEditionMatrix` -> prints effective feature matrix per edition

