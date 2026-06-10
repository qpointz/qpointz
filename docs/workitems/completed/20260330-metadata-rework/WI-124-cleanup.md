# WI-124 — Cleanup: Legacy Format Removal, Config Keys, Java `@ConfigurationProperties`

**Story:** Metadata Rework
**Spec sections:** §14, §15
**Depends on:** WI-123 (primary feature surface stable)

## Objective

Remove **legacy** file **format** implementations and obsolete configuration keys; sweep dead code; ensure all **`mill.metadata.*`** keys are bound by **Java** `@ConfigurationProperties` for Spring metadata generation.

**Breaking:** **`mill.metadata.storage.*`** is **removed**; implementations use **`mill.metadata.repository.*`** (SPEC §14.0). **`mill.metadata.repository.type=file`** remains valid with **canonical YAML** only (§15).

## Scope

### 1. File repository (SPEC §15)

Delete **old** ad-hoc file repository classes and tests that target the **legacy** on-disk format only.

**Keep / implement** the **canonical YAML** file adapters in `mill-metadata-persistence` and serializers in `mill-metadata-core` per SPEC §15.

Wire file backend when **`mill.metadata.repository.type=file`** (not `storage`).

### 2. `mill.metadata.repository.*` configuration (SPEC §14, §15.7)

Valid values: **`mill.metadata.repository.type`** = `jpa` | `file` | `noop`.

File keys: **`mill.metadata.repository.file.path`**, **`.writable`**, **`.watch`**.

Remove all **`mill.metadata.storage.*`** references from autoconfigure conditionals, samples, and tests.

### 3. Java `@ConfigurationProperties` (SPEC §14)

All metadata property classes in `mill-metadata-autoconfigure` that this story touches must be **Java** so `spring-boot-configuration-processor` emits `META-INF/spring-configuration-metadata.json`.

Minimum set includes (exact class names TBD in implementation):

| Key prefix | Notes |
|------------|--------|
| `mill.metadata.repository` | `type`, nested `file.*` |
| `mill.metadata.seed` | ordered resources (**WI-126**); may ship in same module |
| `mill.metadata.import` | if present |

### 4. Dead code sweep

After WI-120–WI-123 complete: grep and remove remaining coordinate-era types, old service names, and **`metadata_promotion`** references.

Run `./gradlew :metadata:build` — zero avoidable warnings.

### 5. Documentation standards (STORY locked #15)

- **Kotlin/Java:** KDoc/JavaDoc to **parameter** level on all touched production code in this WI.
- **TypeScript (UI):** JSDoc/TSDoc to **function** level on touched exports.

## Done Criteria

- Legacy file **format** code paths deleted; **canonical YAML** file backend works under **`mill.metadata.repository.type=file`**.
- **No** remaining **`mill.metadata.storage.*`** in code or normative docs.
- `spring-configuration-metadata.json` contains **`mill.metadata.repository.*`** (and **`mill.metadata.seed.*`** if implemented).
- **`metadata_promotion`** references gone from `metadata/*`.
- `./gradlew :metadata:build` is clean.
- `./gradlew :metadata:mill-metadata-core:dependencies` shows no `data/*` or Spring artifacts.
