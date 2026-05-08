# WI-250 — Export format SPI (`mill-data-source-core`)

Status: `planned`  
Type: `feature`  
Area: `data`  
Backlog refs: **P-36**, **D-7**

## Goal

Define a **contract** for **streaming export** in [`mill-data-source-core`](../../../../data/mill-data-source-core) (memory definition): format **id**, **MIME type**, **default file extension**, and an encoder that consumes an existing **`VectorBlockIterator`** and writes encoded bytes **directly to the supplied `OutputStream`** as blocks arrive.

**No query execution in format code:** format implementations **must not** call [`DataOperationDispatcher`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java), build **`QueryRequest`**, or parse SQL. They only accept **results** (`VectorBlockIterator` (+ schema metadata if required by the encoder)). Execution is **`ExportVectorBlockSource`** (**WI-252**) + **`ExportFacility`** (**WI-253**).

**No static format lists:** Mill code **must not** hard-code the set of export format ids (no compile-time enum or map owned by `mill-export-service`). **Every** format is contributed by a **provider class** registered via **`META-INF/services/...`** and surfaced through the registry below. Dropping or adding a format JAR + SPI file changes the runtime catalogue without editing export service sources — this is the **dynamic exporter** extension model (classpath / “drop-in” modules).

**Streaming requirement:** Implementations must **not** materialize the entire result set in memory (no `List` of all rows, no single `String` or `byte[]` for the whole file). Working memory must be **bounded** by iterator batch size and small per-format buffers. (Framing for JSON array / Avro container is allowed if growth per step is **O(batch)**, not **O(N rows)** retained.)

## Scope

1. Add contract types in [`mill-data-source-core`](../../../../data/mill-data-source-core) (Kotlin or Java per module convention; no `Mill` prefix on type names): **provider SPI** (e.g. `ExportFormatProvider`) and an **`ExportFormatRegistry`** interface (lookup by id, iterate descriptors for catalog metadata).
2. **SPI registration:** each format JAR ships **`META-INF/services/<provider interface FQN>`** listing one or more provider implementation classes (standard Java `ServiceLoader` discovery).
3. **`mill-data-autoconfigure`:** add a Spring **`@Bean` `ExportFormatRegistry`** (or dedicated `@AutoConfiguration`) that **composes** providers via **`ServiceLoader.load(ExportFormatProvider.class)`** from the application **classpath** (same classloader as the service). **`mill-export-service` / `ExportFacility` inject this bean** — they do **not** construct their own static map. Document duplicate-id behaviour (recommend **fail-fast** at context refresh). **HTTP allowlist** filtering (**`mill.data.services.export.formats`**, **WI-253**) is **not** implemented inside the registry bean: the bean reflects **all** SPI providers; the export service computes the **effective** set for endpoints.
4. Document which formats are **streaming-capable**; **Parquet** explicitly out of scope for MVP (seekable / non-streaming writer constraints).

## Acceptance

- At least one **unit test** proves SPI loads in a minimal test fixture; a second test (autoconfigure or export slice) proves the **registry bean** sees providers from **`META-INF/services`** without a hard-coded id list in export service code.
- Design note or KDoc states that **new exporter modules** are added by **Gradle dependency + provider + `META-INF/services`** — no change to `mill-export-service` sources.

## Depends on

None (first WI in story).
