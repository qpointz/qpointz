# WI-011: Arrow Format Support

**Type:** feature
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `feat/arrow-support`

---

## Goal

Deliver Arrow format support in the source lane (Kotlin), so Arrow IPC data can
be read via existing source abstractions before Arrow Flight/Flight SQL work.

This work item also standardizes data-format module naming under
`data/formats` before adding Arrow.

---

## Scope

This work item implements backlog item `S-16` and includes a preparatory
module-name cleanup:

- Rename existing format modules to `mill-data-format-<name>` convention
- Add new Arrow format module in Kotlin under `data/formats`
- Implement Arrow reader/handler support for IPC file/stream ingestion
- Implement Arrow writer support required by the format module contract/tests
- Map Arrow schema/types to Mill logical types with documented conventions
- Integrate Arrow format with source/calcite discovery and query flow
- Add unit/integration tests
- Update user-facing documentation for Arrow format usage and mapping

---

## Out of Scope

- Arrow Flight server endpoints (tracked separately)
- Arrow Flight SQL command/metadata protocol (tracked separately)
- Proto/schema timezone extension across all contracts and components
  (tracked separately as backlog `P-29`)
- End-to-end frontend-to-backend timezone propagation and UX support
  (tracked separately as backlog `P-30`)
- Cross-module transport protocol refactoring
- Cross-language transport optimization and benchmarking

---

## Implementation Plan

1. **Phase 0: Normalize format module naming**
   - Rename existing modules under `data/formats`:
     - `mill-source-format-text` -> `mill-data-format-text`
     - `mill-source-format-excel` -> `mill-data-format-excel`
     - `mill-source-format-avro` -> `mill-data-format-avro`
     - `mill-source-format-parquet` -> `mill-data-format-parquet`
   - Update all Gradle/settings/module references to new project names
   - Keep package names unchanged unless explicitly needed for build/runtime

2. **Phase 1: Add Arrow format module (Kotlin)**
   - Add `data/formats/mill-data-format-arrow`
   - Wire Gradle/build-logic conventions and minimal Arrow dependencies
   - Register module in `settings.gradle.kts` and aggregate build references

3. **Implement Arrow format components**
   - `ArrowFormatDescriptor` (`type: arrow`)
   - `ArrowDescriptorSubtypeProvider` (Jackson subtype SPI)
   - `ArrowFormatHandlerFactory` (FormatHandlerFactory SPI)
   - `ArrowFormatHandler` (schema inference + record source creation)
   - `ArrowRecordSource` (read Arrow IPC as row source)
   - `ArrowRecordWriter` (write records to Arrow IPC for contract/tests)
   - SPI registrations in `META-INF/services/*`

4. **Implement type mapping contract (baseline)**
   - Numeric: `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE`
   - Logical/scalar: `BOOL`, `STRING`, `BINARY`, `UUID`
   - Temporal: `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ`
   - Intervals: `INTERVAL_DAY`, `INTERVAL_YEAR` with explicit conventions
   - Enforce nullability and deterministic mapping behavior in tests

5. **Handle timezone convention**
   - Follow Arrow per-column timezone semantics (`timestamp(unit, timezone)`)
   - Reader is zone-aware and normalizes timestamp-with-timezone values to UTC
     instant representation internally
   - Do not introduce companion timezone string columns in this work item
   - Cover timezone/no-timezone field behavior in mapping tests

6. **Integrate with source lane**
   - Register Arrow format so source table mapping can discover and read it
   - Verify compatibility with `mill-data-source-core` and calcite source layer
   - Ensure Arrow-backed data is queryable through existing execution path

7. **Testing and validation**
   - Unit tests for type mapping (including edge cases and nullability)
   - Parser tests for Arrow IPC file and stream variants
   - Writer tests for Arrow IPC output round-trip compatibility
   - SPI wiring tests (`DescriptorSubtypeProvider`, `FormatHandlerFactory`)
   - Integration test proving end-to-end query against Arrow-backed source
   - Negative tests for unsupported/ambiguous schema cases

8. **Documentation updates (user-facing)**
   - Add `docs/public/src/sources/formats/arrow.md`
   - Add Arrow to supported-format lists:
     - `docs/public/src/sources/index.md`
     - `docs/public/src/sources/configuration.md`
   - Add Arrow page to nav in `docs/public/mkdocs.yml`
   - Document scope note: format support now, Flight/Flight SQL later
   - Add **Current limitations** section for timezone handling:
     - per-column timezone semantics only
     - no per-row timezone representation
  - full cross-contract timezone propagation tracked by `P-29` / `P-30`

---

## Deliverables

- Renamed format modules using `mill-data-format-*` naming
- New module: `data/formats/mill-data-format-arrow` (Kotlin)
- Arrow-to-Mill mapping implementation with documented conventions
- Arrow timezone handling aligned to per-column Arrow semantics (no companion
  timezone columns)
- Unit/integration/SPI test coverage for Arrow format support
- User documentation updated with Arrow format page and references
- Updated backlog/milestone references after implementation completion

---

## Verification

1. All `data/formats` module names follow `mill-data-format-<name>`.
2. Build compiles with updated project references after rename.
3. Arrow format module builds and tests pass.
4. Arrow IPC file/stream inputs are readable via source abstractions.
5. Arrow writer round-trip tests pass.
6. Baseline mapping contract is covered by deterministic tests.
7. Temporal and interval mappings follow documented conventions.
8. Arrow timezone behavior is validated as per-column only, with zone-aware
   UTC normalization in reader path.
9. Arrow format handling does not introduce companion timezone string columns.
10. Arrow sources are queryable through existing calcite/backend flow.
11. User docs include Arrow format page and navigation links, including a
    **Current limitations** subsection for timezone.
12. `./gradlew test` passes for all affected modules.

## Estimated Effort

Large. Includes cross-module rename + new Kotlin Arrow format implementation,
SPI wiring, mapping semantics, tests, and public documentation updates.
