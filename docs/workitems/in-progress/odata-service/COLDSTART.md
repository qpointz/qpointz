# Cold start — odata-service

**Audience:** implementer with no prior context on this story.  
**Branch:** `feat/odata-service` (rebase on `origin/dev` before MR).  
**Design:** [`docs/design/platform/odata-service.md`](../../../design/platform/odata-service.md)  
**Backlog:** P-41 in [`BACKLOG.md`](../../BACKLOG.md)

## Read order

1. This file (setup + file map + verify commands)
2. [`STORY.md`](STORY.md) — constraints, architecture decisions, WI order
3. [`odata-service.md`](../../../design/platform/odata-service.md) — normative contract
4. Current WI file — implement one WI, check boxes in `STORY.md`, commit, next WI

## Branch setup

```bash
git fetch origin
git checkout feat/odata-service
# fresh branch from dev:
# git checkout -b feat/odata-service origin/dev
```

Working directory: **repository root** (where `settings.gradle.kts` and `./gradlew` live).

## Preconditions

| Check | Detail |
|-------|--------|
| JDK / Gradle | **Java 25** (WI-324); root `./gradlew` |
| Skymill fixture | `test/datasets/skymill/` (parquet + `skymill-meta-repository.yaml`) |
| WI-325 | Design doc + this story folder — **done** |

## Story exit criteria (all WIs)

```bash
./gradlew :data:mill-data-odata:test
./gradlew :data:mill-data-backends:test --tests "*RelToSubstrait*"
./gradlew :services:mill-data-odata-service:test
./gradlew :services:mill-data-odata-service:testIT
./gradlew :apps:mill-service:compileJava
```

**Integration check:** OData read path produces a Substrait `Plan` and executes via `DataOperationDispatcher` (same rewrite chain as export/gRPC plan path).

## Bootstrap sequence (matches WIs)

| Step | WI | Action |
|------|-----|--------|
| 0 | — | WI-325 docs (**done**) |
| 1 | WI-324 | Java 25 toolchain + CI/Docker; full `./gradlew build` |
| 2 | WI-326 | `include(":data:mill-data-odata")`; RWS `EntityDataModel`; `:data:mill-data-odata:test` |
| 3 | WI-327 | Adapter + RWS AST→`ODataExpressionToRex`, `ODataRelComposer`; Skymill converter gate tests |
| 4 | WI-328 | `include(":services:mill-data-odata-service")`; RWS `odata_controller`; `mill-service` wiring; testIT |
| 5 | WI-329 | Public + platform doc sweep |

## Gradle registration

Add to [`settings.gradle.kts`](../../../../settings.gradle.kts):

```kotlin
include(":data:mill-data-odata")
include(":services:mill-data-odata-service")
```

[`apps/mill-service/build.gradle.kts`](../../../../apps/mill-service/build.gradle.kts):

```kotlin
implementation(project(":services:mill-data-odata-service"))
```

[`apps/mill-service/src/main/resources/application.yml`](../../../../apps/mill-service/src/main/resources/application.yml)
(under `mill.data.services`):

```yaml
      odata:
        enable: true
        external-host: http-request
        # optional: default-scope, max-top
```

## Package layout

| Module | Base package |
|--------|----------------|
| `mill-data-odata` | `io.qpointz.mill.data.odata` |
| `mill-data-odata-service` | `io.qpointz.mill.data.odata.service` |

**Language:** Kotlin for implementation; **Java only** for `@ConfigurationProperties`
(`ODataServiceProperties` → `mill.data.services.odata.*`).

## Module skeleton

### WI-326 — `mill-data-odata`

```
data/mill-data-odata/
  build.gradle.kts
  src/main/kotlin/io/qpointz/mill/data/odata/
    edm/              EntityDataModelFactory, EntitySetNaming, NavigationPropertyBuilder
    type/             MillTypeToEdmMapper
    facet/            DescriptiveAnnotationMapper
  src/test/kotlin/io/qpointz/mill/data/odata/
    edm/EntityDataModelFactoryTest.kt
    resources/        fixture schema YAML fragments
```

`build.gradle.kts` pattern: [`data/mill-data-query/build.gradle.kts`](../../../../data/mill-data-query/build.gradle.kts) —
Kotlin + `io.qpointz.plugins.mill`, **no** Spring.

```kotlin
dependencies {
    api(project(":data:mill-data-backend-core"))
    implementation(project(":data:mill-data-schema-core"))
    // RWS/SDL com.sdl 2.16.1 in libs.versions.toml (WI-326/328)
    implementation(libs.sdl.odata.api)
    implementation(libs.sdl.odata.edm)
}
```

### WI-327 — query bridge + adapter

```
data/mill-data-odata/
  build.gradle.kts          # add: implementation(project(":data:mill-data-backends"))
  src/main/kotlin/io/qpointz/mill/data/odata/
    resolve/          EdmPropertyResolver
    expr/             ODataExpressionToRex
    plan/             ODataRelComposer, ODataQueryOptions
    read/             ODataEntityReader
    exec/             ODataQueryExecutor
  src/test/kotlin/.../
  src/test/resources/odata-filters/

data/mill-data-backend-core/
  src/main/java/io/qpointz/mill/data/backend/
    SubstraitPlanExecutor.java       # Plan → dispatcher (extract from export pattern)

data/mill-data-backends/
  src/main/java/.../calcite/
    RelBuilderFactory.java
    CalciteRelBuilderFactory.java
    RelToSubstraitPlanConverter.java
    CalciteRelToSubstraitPlanConverter.java
    RelPlanDispatcherBridge.java
  src/test/java/.../
    RelToSubstraitCompatibilityTest.java   # Skymill T0–T5 gate
```

Implement **`ODataExpressionToRex`** as a visitor over RWS `com.sdl.odata.api.processor.query.*` operators, producing `RexNode` via `RelBuilderFactory`.

**Module rule:** no Calcite types in `mill-data-backend-core`. Adapter + `RelBuilderFactory` in **`mill-data-backends` only**.

### WI-328 — `mill-data-odata-service`

```
services/mill-data-odata-service/
  build.gradle.kts
  src/main/java/io/qpointz/mill/data/odata/
    ODataServiceProperties.java
  src/main/kotlin/io/qpointz/mill/data/odata/
    ODataServiceDescriptor.kt
    ODataConnectionDescriptor.kt
    config/ODataWebAutoConfiguration.kt
    datasource/MillODataDataSource.kt
    config/RwsODataControllerConfiguration.kt
  src/testIT/kotlin/.../ODataSkymillIT.kt
```

RWS deps: `odata_controller`, `odata_parser`, `odata_renderer`, `odata_processor` (`com.sdl` **2.16.1**). Mount controller at `/services/odata/v4/`.

`build.gradle.kts` pattern: [`services/mill-data-query-service/build.gradle.kts`](../../../../services/mill-data-query-service/build.gradle.kts).

Wire `RelPlanDispatcherBridge` / `RelBuilderFactory` from `mill-data-autoconfigure` (new beans in WI-327).

## Adapter pipeline (WI-327)

```text
ODataRelComposer.compose() → RelRoot
  → RelPlanDispatcherBridge          [mill-data-backends]
      → RelToSubstraitPlanConverter  [SubstraitRelVisitor — same as CalciteSqlProvider]
      → SubstraitPlanExecutor        [mill-data-backend-core]
          → DataOperationDispatcher (+ PlanRewriteChain)
```

| Type | Module | Role |
|------|--------|------|
| `RelBuilderFactory` | backends | Injected into `ODataRelComposer` |
| `RelToSubstraitPlanConverter` | backends | RelRoot → `Plan` |
| `RelPlanDispatcherBridge` | backends | Converter + executor |
| `SubstraitPlanExecutor` | backend-core | `Plan` → `VectorBlockIterator` |
| `ODataQueryExecutor` | mill-data-odata | OData orchestration |

**Do not** open `CalciteContextFactory` from OData — inject facades only.

## Class responsibilities

| Class | Module | Role |
|-------|--------|------|
| `EntityDataModelFactory` | mill-data-odata | RWS `EntityDataModel` from `SchemaFacetService` |
| `MillTypeToEdmMapper` | mill-data-odata | `DatabaseType` → RWS Edm primitive |
| `EdmPropertyResolver` | mill-data-odata | EDM property → column / RexInputRef |
| `ODataExpressionToRex` | mill-data-odata | RWS query AST → `RexNode` |
| `ODataRelComposer` | mill-data-odata | URI options → `RelRoot` via `RelBuilderFactory` |
| `ODataEntityReader` | mill-data-odata | `VectorBlock` → row maps for renderer |
| `ODataQueryExecutor` | mill-data-odata | Compose + delegate to bridge |
| `RelPlanDispatcherBridge` | backends | Reusable RelNode execution entry |
| `MillODataDataSource` | odata-service | RWS `DataSource`; push-down to executor |

## OData query options (v1)

| Option | Support |
|--------|---------|
| `$filter` | EDM → `RexNode` |
| `$select` | `RelBuilder.project` |
| `$orderby` | `RelBuilder.sort` |
| `$top` / `$skip` | Fetch / offset (`max-top` cap) |
| `$expand` | `RelBuilder.join` from `RelationFacet` keys |
| `$count` | Deferred (v1.1) |

## Skymill converter gate (WI-327 unit tests)

| Tier | Scenario |
|------|----------|
| T0 | Scan only (`skymill_cities`) |
| T1 | Single `$expand` (`cities_segments_origin`) |
| T2 | Dual expand to same table (origin + destination) |
| T3 | Chain `bookings` → `passenger` → `cities` |
| T4 | Long chain through `flight_instances` → `segments` |
| T5 | Expand + `$filter` on navigation path |

Converter throws → OData must map to **400**, not full scan.

## testIT scenarios (WI-328)

1. **Metadata:** `GET /services/odata/v4/$metadata` → 200, entity sets `skymill_*`.
2. **Entity read:** `GET .../Mill/skymill_cities` → 200, JSON rows.
3. **Filter:** `$filter` on known column → expected subset.
4. **Expand:** e.g. `cities` → `cities_segments_origin` → nested JSON.
5. **Dispatcher path:** execution goes through `DataOperationDispatcher` with `plan` set (optional spy).

Reference IT: [`MillGrpcSkymillQueryIT.java`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java).

## Conventions to copy

| Need | Follow |
|------|--------|
| Spring-free core | `data/mill-data-query/` |
| Plan → dispatcher | `ExportVectorBlockSource` |
| HTTP + descriptors | `services/mill-data-query-service/` |
| Rel→Substrait | `CalciteSqlProvider.parseSql` |
| `ConditionalOnService` | `ExportRestController` |
| Facet merge tests | `SchemaFacetServiceSkyMillIT.kt` |

## WI workflow (per WI: verify → tracker → commit → push → CI)

| Step | WI | Verify (local) | Then |
|------|-----|----------------|------|
| 1 | WI-324 | `./gradlew build` (JDK 25) | tracker → commit → push → **CI green** |
| 2 | WI-326 | `./gradlew :data:mill-data-odata:test` | same |
| 3 | WI-327 | `:data:mill-data-odata:test` + `:data:mill-data-backends:test --tests "*RelToSubstrait*"` | same |
| 4 | WI-328 | odata-service `test` + `testIT`; `:apps:mill-service:compileJava` | same |
| 5 | WI-329 | docs + full exit criteria in § Story exit criteria | same → **open GitLab MR** |

### Checklist after every WI (324, 326–329)

**Gate:** next WI starts only after this checklist is fully done.

1. Tests / compile for that WI pass locally.
2. **Tracker** — `STORY.md` mark WI `[x]`; update `WI-NNN-*.md` (status / acceptance).
3. **Commit** — `git add` entire WI changeset (code + tracker files); **one commit** per WI (`[change]` / `[feat]` / `[docs]`).
4. **Push** — `git push origin feat/odata-service` (one WI per push).
5. **CI** — GitLab pipeline **green** on that push before continuing.

### After WI-329 (last WI only)

- Final push with green CI.
- Create MR: **`feat/odata-service` → `dev`** on [GitLab](https://gitlab.qpointz.io/qpointz/qpointz/-/merge_requests/new?merge_request%5Bsource_branch%5D=feat%2Fodata-service).
- Story archive / milestone / history squash — only when user requests closure per [`RULES.md`](../../RULES.md).

Prefix: `[change]` for WI-324, `[feat]` for code WIs, `[docs]` for WI-329. **One logical commit per WI** per [`RULES.md`](../../RULES.md).

## Common pitfalls

- SQL string predicates — use `RexNode`.
- `RelToSubstraitPlanConverter` in backend-core — **forbidden** (no Calcite).
- `api(project(":data:mill-data-backends"))` on odata — use **`implementation`** only.
- Kotlin `@ConfigurationProperties` — use Java for `ODataServiceProperties`.
- Skipping WI-324 before RWS deps — **2.16.1 requires JDK 25**.
- Skipping Skymill converter gate before HTTP testIT.

## Deferred (follow-on stories)

| Slug | Content |
|------|---------|
| `odata-v2-compat` | OData v2 for legacy Excel |
| `odata-filter-functions` | Extended `$filter` functions |
| `relnode-execution-api` | Substrait migration — RelNode-only dispatcher |
| `mill-data-plan-compose` | Extract shared compose module if second RelNode consumer appears |
| `odata-bi-certification` | Power BI / Tableau client matrix |
