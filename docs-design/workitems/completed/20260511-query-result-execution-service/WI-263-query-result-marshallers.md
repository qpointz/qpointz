# WI-263 — Result marshaller SPI + built-in formats

Status: `done`  
Type: `feature`  
Area: `data`  
Backlog refs: **D-8**

**Story:** [`STORY.md`](STORY.md) — **WI-263** (tracker row 2). **Delivery:** when this WI is finished, mark its tracker + **[`STORY.md`](STORY.md)** **Tracker** / **Work Items**, then **one commit** for the full tree (**[`RULES.md`](../../RULES.md)**).

## Language

- **Kotlin** — **`ResultMarshaller`**, registry, SPI types, built-ins, and Spring **`@Configuration` / `@Bean`** that assemble **`ServiceLoader` → `ResultMarshallerRegistry`** (in **`mill-data-autoconfigure`** and/or **`mill-data-query-service`**).
- **Java** — **only** if this WI introduces a **`@ConfigurationProperties`** type (unlikely for **WI-263** alone); prefer keeping all **WI-263** code in **Kotlin** per **[`STORY.md`](STORY.md)** **Implementation conventions**.

## Tracker (this WI)

- [x] **`ResultMarshaller`** contract + **`ResultMarshallerRegistry`** (**Kotlin**, Spring-free **`mill-data-query`**) — lookup by **format id**; each marshaller declares **`Content-Type`** and **`Accept`**-matchable types (**standard IANA MIME only** — see **[`STORY.md`](STORY.md)** **Format negotiation**); deterministic behaviour on **duplicate ids** (fail fast or last-wins — pick one and test); **KDoc** to **parameter** level on all new/changed production API
- [x] **JVM `ServiceLoader` SPI** for contributions: **`ResultMarshallerProvider`** (or equivalent) declared under **`META-INF/services/`**; **Kotlin** provider implementations return one or more **`ResultMarshaller`** instances (built-ins and third-party JARs use the **same** mechanism)
- [x] **Spring-assembled registry bean** — **`ServiceLoader.load(...)`** at context startup (in **`mill-data-autoconfigure`** and/or **`mill-data-query-service`** `@Configuration`), build **`ResultMarshallerRegistry`**, expose as **`@Bean`**; **`QueryResultExecutionService`** / REST layer receive the registry via injection (**no** marshaller **`@Bean`** per format — **SPI only** for format plugins unless tests use **`@Primary`** / manual registry)
- [x] **Blocking** encode: **`OutputStream`** / **`WritableByteChannel`** and/or **`Consumer<ByteBuffer>`**
- [x] **`rows-objects`** + **`rows-compact-batch`** implementations + tests (no **`Flux`** in **core**)
- [x] Document how **`defaultFormat`** / **`GET`** overrides align with paged **`GET /api/v1/query/{executionId}?pageIndex=…`** responses (**WI-264** wires HTTP)

## Discovery and registry (locked)

| Layer | Responsibility |
|--------|----------------|
| **`mill-data-query`** | **`ResultMarshaller`**, **`ResultMarshallerProvider`** (SPI type), **`ResultMarshallerRegistry`** — **Kotlin**, **no Spring** imports; **KDoc** to **parameter** level |
| **`mill-data-autoconfigure`** / **`mill-data-query-service`** | **Kotlin** **`@Configuration`** / **`@Bean`** runs **`ServiceLoader`**, builds registry, registers **`ResultMarshallerRegistry`**; **Java** **only** for **`@ConfigurationProperties`** (see **[`STORY.md`](STORY.md)**); optional **`@ConditionalOnMissingBean`** only if a test-friendly override is needed |

**Extensibility:** a new flavour ships as a **dependency JAR** with **`META-INF/services/…ResultMarshallerProvider`** (and provider implementation). No change to session core except format id collision rules. **`mill-data-query`** built-ins register via the **same SPI** files in that module’s resources.

## Goal

Pluggable **`ResultMarshaller`** via **SPI**, assembled into a single **`ResultMarshallerRegistry`** **Spring bean**: **few** built-ins, **easy extension** (add JAR + provider).

Initial formats (both **`Content-Type: application/json`**; distinguished by **`format`** / **`defaultFormat`**, not by MIME):

- **`rows-objects`** — associative row objects (Analyze / current UI expectation)
- **`rows-compact-batch`** — schema + dense value arrays per response slice

**Encode API (framework-free in core):** each marshaller writes through **blocking** **`OutputStream`** / **`WritableByteChannel`** and/or **`Consumer<ByteBuffer>`** chunk callbacks so large pages avoid materializing a single giant **`String`**.

**HTTP / reactive adapters (WI-264 scope overlap allowed):** thin **`StreamingResponseBody`**, **`Flux<DataBuffer>`**, or pipe-to-**`InputStream`** bridges live only in **`mill-data-query-service`** — **no Reactor dependency in `mill-data-query`**.

Binding: **`defaultFormat`** optional on **`create`/`replace`**; paged **`GET`** on **`/api/v1/query/{executionId}`** (with **`pageIndex`**) uses **`format`** / **`Accept`** / session default per **[`STORY.md`](STORY.md)** **Format negotiation** (HTTP mapping and errors in **WI-264**; docs + tests in **WI-265**).

## Scope

1. Interfaces + **`ResultMarshallerRegistry`** in **`mill-data-query`** (blocking sinks + optional small DTO helpers).
2. **`ResultMarshallerProvider`** SPI + **`META-INF/services`** entries for **`rows-objects`** and **`rows-compact-batch`**.
3. **Kotlin** Spring **`@Configuration`** (autoconfigure and/or **`mill-data-query-service`**) that **loads SPI → registry bean**; wire registry into **`QueryResultExecutionService`** (WI-262) once that API accepts a registry dependency (**Java** not required here unless you colocate **`@ConfigurationProperties`**).
4. Coordinate with **WI-264** for HTTP streaming entry points that call marshaller sinks and resolve **`format`** / **`Accept`** against the registry.

## Acceptance

- At least **two** SPI-registered implementations covered by tests (round-trip or golden payloads / stream bytes); each reports **`application/json`** (or other **standard** MIME) via the marshaller metadata API used by the registry for **`Accept`** matching.
- Integration or slice test proving **Spring** context loads **both** built-ins via **`ServiceLoader`** into one registry bean (duplicate-id failure or override policy asserted).
- **KDoc** complete on SPI surface + registry (**parameter** level).
- Document extension points (**SPI package name**, provider interface, **`META-INF/services`** path) in **WI-265** design doc.

## Depends on

**WI-262** (page slices and **`VectorBlock`** views produced by core).

## Notes

Reuse vector-to-cell mapping conventions consistent with **`mill-export-service`** patterns where practical.

**MIME policy:** built-ins use **standard** types only; multiple JSON encodings sharing **`application/json`** are OK — disambiguation is always **`format`** id (or **`defaultFormat`**), never a vendor subtype. Third-party SPI marshallers **must** use real IANA types for their **`Content-Type`** declarations.
