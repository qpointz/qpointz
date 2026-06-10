# WI-126 — Startup Metadata Seed (`mill.metadata.seed`)

**Story:** Metadata Rework
**Spec sections:** §14.1, §6.6, §7.5, §11
**Depends on:** WI-122 (`metadata_seed` table + JPA ledger access), WI-120 (`MetadataImportService` / domain stable)

## Objective

Replace ad-hoc on-startup metadata loading with an **explicit**, **ordered**, **ledger-backed** seed pipeline so each configured resource runs **at most once** per environment (survives restarts).

## Scope

### 1. Core contract (`mill-metadata-core`)

- Define / finalise **`MetadataSeedLedgerRepository`** (SPEC §6.6) — **no Spring** types on the interface.
- Stable **`seed_key`** derivation (e.g. index + normalised resource string) or explicit id per list entry — document in KDoc.

### 2. Persistence (`mill-metadata-persistence`)

- JPA entity + Spring Data repository for **`metadata_seed`** (columns per SPEC §8.3 / §14.1).
- Implement **`MetadataSeedLedgerRepository`**.

### 3. Autoconfigure (`mill-metadata-autoconfigure`)

- **Java** `@ConfigurationProperties` for **`mill.metadata.seed`** (list of resource locations, failure policy if needed).
- **`ApplicationRunner`** (or equivalent) that:
  1. Iterates configured resources **in order**.
  2. Skips entries already marked completed in the ledger.
  3. Parses YAML using the **same path as import** (`MetadataImportService` / canonical format per SPEC §11 / §15).
  4. On success, records completion in **`metadata_seed`**.
- **Fail-fast** vs **continue** — follow SPEC §14.1 (default recommendation: fail-fast unless documented otherwise).

### 4. Documentation standards

- **Java:** JavaDoc to **parameter** level on new/changed types.

## Done Criteria

- Second application start does **not** re-apply completed seeds (integration test with H2).
- **`mill.metadata.seed`** appears in **`spring-configuration-metadata.json`**.
- No scattered one-off classpath metadata loads remain for the same concern (grep / review).
- `./gradlew :metadata:build` passes for touched modules.
