# API Documentation Generation

**Status:** Active  
**Last Updated:** February 2026

---

## Quick Start

Build the full documentation site (MkDocs pages + Dokka API reference):

```bash
make docs-build
```

This runs three steps:
1. `./gradlew dokkaGenerate` — generates the aggregated Dokka HTML to `build/dokka/html/`
2. `python -m mkdocs build` — builds the MkDocs site to `docs/public/site/`
3. Copies the Dokka output into `docs/public/site/api/kotlin/`

To preview locally with live reload:

```bash
make docs-serve
```

Generate API docs only:

```bash
./gradlew dokkaGenerate
```

Output lands at `build/dokka/html/`. Open `index.html` in a browser to preview the API reference standalone.

Generate docs for a single module:

```bash
# Dokka HTML (browsable)
./gradlew :core:mill-core:dokkaGeneratePublicationHtml

# Javadoc (for JAR packaging)
./gradlew :core:mill-core:dokkaGeneratePublicationJavadoc
```

---

## Architecture

### Why Dokka Output Lives Outside MkDocs `src/`

Dokka generates deeply nested HTML file paths based on package and class names. On Windows, some paths exceed the 260-character `MAX_PATH` limit, which causes `mkdocs build` to fail with `FileNotFoundError` when copying static files. Additionally, processing ~5,000 Dokka HTML files through MkDocs is unnecessary and slow.

The solution is a **two-phase build**:

1. **MkDocs** builds the main documentation site from `docs/public/src/` (fast, markdown-only).
2. **Dokka HTML** is generated into `build/dokka/html/` (standard Gradle output, gitignored).
3. A **post-build copy** merges the Dokka output into the MkDocs site at `site/api/kotlin/`.

The MkDocs `nav` entry `api/kotlin/index.html` creates a direct link to the Dokka site within the final assembled output.

### Two Dokka Plugins

| Plugin | Output format | Aggregation | Purpose |
|--------|--------------|-------------|---------|
| `org.jetbrains.dokka` | Modern HTML | Yes (multi-module) | Browsable API docs site |
| `org.jetbrains.dokka-javadoc` | Traditional Javadoc HTML | No (per-module only) | Javadoc JARs for Maven Central |

Both plugins process **Java and Kotlin sources**. Dokka is not Kotlin-only.

### Version Management

The root `build.gradle.kts` declares both plugins with explicit versions:

```kotlin
plugins {
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0" apply false
}
```

The `dokka-javadoc` plugin is declared `apply false` at the root because it does not support aggregation. It is only applied in leaf modules for per-module Javadoc generation.

All subprojects inherit the version and declare plugins without a version number:

```kotlin
plugins {
    id("org.jetbrains.dokka")           // leaf + aggregate modules
    id("org.jetbrains.dokka-javadoc")   // leaf modules only
}
```

### Aggregation Hierarchy

Dokka HTML aggregation is wired via `dokka(project(...))` dependencies:

```
root build.gradle.kts
├── dokka(project(":ai"))       -> ai/build.gradle.kts
│   ├── dokka(project(":ai:mill-ai-v1-core"))
│   ├── dokka(project(":ai:mill-ai-v1-nlsql-chat-service"))
│   └── ...
├── dokka(project(":core"))     -> core/build.gradle.kts
│   ├── dokka(project(":core:mill-core"))
│   ├── dokka(project(":core:mill-security"))
│   └── ...
├── dokka(project(":data"))     -> data/build.gradle.kts
│   ├── dokka(project(":data:mill-data-service"))
│   ├── dokka(project(":data:formats:mill-source-format-parquet"))
│   └── ...
├── dokka(project(":metadata")) -> metadata/build.gradle.kts
├── dokka(project(":apps"))     -> apps/build.gradle.kts
├── dokka(project(":clients"))  -> clients/build.gradle.kts
└── dokka(project(":ui"))       -> ui/build.gradle.kts
```

**Aggregate modules** (ai, apps, clients, core, data, metadata, ui) apply only `org.jetbrains.dokka` and declare `dokka(project(...))` dependencies for their children.

**Leaf modules** apply both `org.jetbrains.dokka` and `org.jetbrains.dokka-javadoc`. They do not declare `dokka(...)` dependencies.

### Output Directories

| Output | Path | Contents |
|--------|------|----------|
| Aggregated HTML site | `build/dokka/html/` | All modules, cross-linked, navigable |
| Final docs site | `docs/public/site/` | MkDocs pages + API docs merged |
| API docs in site | `docs/public/site/api/kotlin/` | Copied from `build/dokka/html/` post-build |
| Per-module HTML | `{module}/build/dokka/html/` | Single module, standalone |
| Per-module Javadoc | `{module}/build/dokka/javadoc/` | Single module, for JAR packaging |

The aggregated HTML output directory is configured in the root `build.gradle.kts`:

```kotlin
dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}
```

---

## MkDocs Integration

### `mkdocs.yml` Navigation

The API docs link in `docs/public/mkdocs.yml` points to the Dokka output:

```yaml
nav:
    - Api:
        - Kotlin: api/kotlin/index.html
```

During `mkdocs build`, this produces a WARNING because the file doesn't exist in `src/`. This is expected — the file is copied into `site/` as a post-build step.

### Build Process (Local)

```bash
make docs-build
```

Equivalent manual steps:

```bash
./gradlew dokkaGenerate
cd docs/public && python -m mkdocs build
cp -r ../../build/dokka/html docs/public/site/api/kotlin
```

### Build Process (CI)

The CI pipeline in `.gitlab/pipelines/publish.yml` uses two jobs:

1. **`publish:dokka`** — Gradle job that runs `dokkaGenerate` and saves `build/dokka/html/` as an artifact.
2. **`publish:docs`** — Python job that runs `mkdocs build`, copies the Dokka artifact into `site/api/kotlin/`, then deploys with `mike`.

---

## Adding a New Module

1. Add `id("org.jetbrains.dokka")` and `id("org.jetbrains.dokka-javadoc")` to the module's `plugins {}` block:

   ```kotlin
   plugins {
       `java-library`
       id("io.qpointz.plugins.mill")
       id("org.jetbrains.dokka")
       id("org.jetbrains.dokka-javadoc")
   }
   ```

2. In the parent aggregate module's `build.gradle.kts`, add a `dokka(project(...))` dependency:

   ```kotlin
   dependencies {
       dokka(project(":core:my-new-module"))
   }
   ```

3. If creating a **new group** (new aggregate), also add to root `build.gradle.kts`:

   ```kotlin
   dependencies {
       dokka(project(":my-new-group"))
   }
   ```

   And create the group's `build.gradle.kts` with `id("org.jetbrains.dokka")` and `dokka(project(...))` dependencies for its children.

4. Verify: `make docs-build` and check the new module appears in `docs/public/site/api/kotlin/index.html`.

---

## Troubleshooting

### Empty output after `clean`

Dokka module descriptors can become stale. Force full regeneration:

```bash
./gradlew dokkaGenerate --rerun-tasks
```

### Build failure on `clean dokkaGenerate`

Running `clean` and `dokkaGenerate` as a single command can cause race conditions where intermediate files are deleted while being read. Run them separately:

```bash
./gradlew clean
./gradlew dokkaGenerate
```

### Javadoc aggregation produces empty directories

This is by design. Dokka's Javadoc output format does not support multi-module aggregation. Only Dokka HTML aggregates. Javadoc output is per-module only and is used exclusively for Maven JAR publishing.

### Missing module in aggregated site

Ensure the module has `id("org.jetbrains.dokka")` in its plugins and that its parent aggregate has a `dokka(project(...))` dependency pointing to it.

### `mkdocs build` warns about `api/kotlin/index.html`

The warning `A reference to 'api/kotlin/index.html' is included in the 'nav' configuration, which is not found in the documentation files` is expected. The Dokka output is merged into the site as a post-build step. Use `make docs-build` instead of running `mkdocs build` alone.

### Windows path length errors

Dokka generates deeply nested file paths. If you encounter `FileNotFoundError` or path-related failures on Windows, ensure Dokka output goes to `build/dokka/html/` (not inside `docs/public/src/`). The current setup avoids this issue by keeping Dokka output separate from MkDocs source files.
