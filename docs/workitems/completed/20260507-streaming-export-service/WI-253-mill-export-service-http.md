# WI-253 — `mill-export-service`: `ExportFacility`, HTTP API, catalog

Status: `planned`  
Type: `feature`  
Area: `services`, `platform`  
Backlog refs: **P-36**

## Goal

Add **`services/mill-export-service`** registered in **`mill-service`**: base path **`/services/export`**, with a spring **`ExportFacility`** (name as implemented) that **wires**:

1. **[`ExportVectorBlockSource`](./WI-252-export-vector-block-source-backend.md)** (**WI-252**) — produces **`VectorBlockIterator`** from **`POST /sql`** (ad-hoc SQL) or plan-native **`GET /schemas/{schema}/tables/{table}`**.
2. **`ExportFormatRegistry` Spring bean** (**WI-250**, **`mill-data-autoconfigure`**) — **SPI-loaded** (`META-INF/services`); exposes **all** discovered encoders. **HTTP behaviour** further restricts **`?format=`** and catalog listings using **`mill.data.services.export.formats`** (allowlist / wildcard) — see **Configuration** below.

The facility **pumps** iterator → format encoder → HTTP **`OutputStream`** (bounded memory per story rules).

**Path convention:** base **`/services/export`**. **Discovery** uses explicit sub-paths (JSON). **Streaming** uses dedicated resources so **`GET /`** is **not** overloaded between catalog and bytes.

| Method | Path (under base) | Behaviour |
|--------|-------------------|-----------|
| GET | `/catalog` | **JSON** aggregate: root **`formats`** = **effective HTTP set** (**SPI** ∩ **`mill.data.services.export.formats`** rules, or **all** SPI formats when wildcard rules apply) **and** **`schemas`**. Each schema lists **tables**; each table lists **ready-to-use absolute export URLs** for **those** formats only (same **external-host** resolution as HTTP data service). |
| GET | `/formats` | **JSON** list = **same effective HTTP format set** as **`catalog.formats`** (not “hidden” SPI-only formats). |
| GET | `/schemas` | **JSON** list of schemas (minimal: name + optional table count and/or **`href`** to **`/schemas/{schema}`**). |
| GET | `/schemas/{schema}` | **JSON** tables in that schema + **absolute export URLs** per table × format (or equivalent nested structure). |
| POST | `/sql` | Raw **SQL** body; `?format=` & `?filename=`; **streaming** response (SQL parse path on dispatcher via **WI-252**). |
| GET | `/schemas/{schema}/tables/{table}` | **Plan-native** full table scan (**WI-252**); `?format=` & `?filename=`; **streaming** response. |

**Path segments:** document **percent-encoding** for schema/table names that contain reserved characters; controllers must decode safely and return **4xx** on invalid paths.

## Configuration (`mill.data.services.export`)

Bind with a **Java** `@ConfigurationProperties` class (same pattern as [`HttpServiceProperties`](../../../../services/mill-data-http-service/src/main/java/io/qpointz/mill/data/backend/access/http/configuration/HttpServiceProperties.java)) so **`spring-boot-configuration-processor`** emits metadata. Use **`@ConditionalOnService(value = "export", group = "data")`** on export configuration/controllers so **`mill.data.services.export.enable`** participates in the same **enable** convention as **`mill.data.services.http`**.

| Key | Type | Behaviour |
|-----|------|-----------|
| **`enable`** | boolean | When **false** (or omit the whole `export` group per [`OnServiceEnabledCondition`](../../../../core/mill-spring-support/src/main/java/io/qpointz/mill/annotations/service/OnServiceEnabledCondition.java): document expected bootstrap), export controllers/facility beans do not load. When **true**, `/services/export/**` is active. |
| **`external-host`** | String | Same semantics as **`mill.data.services.http.external-host`**: logical key into **`mill.application.hosts.externals`** (see [`HttpConnectionDescriptor`](../../../../services/mill-data-http-service/src/main/java/io/qpointz/mill/data/backend/access/http/configuration/HttpConnectionDescriptor.java)) used when building **absolute** catalog / export URLs. |
| **`formats`** | `List<String>` | **HTTP allowlist** only. **`ExportFormatRegistry`** (SPI) still discovers **all** exporter implementations on the classpath; internally encoders remain available for every registered provider. **Endpoints** (streaming `?format=`, **`GET /formats`**, **`catalog.formats`**, and per-table URL lists) expose **only** formats in the **effective set**: when **not** wildcard, **intersection** of configured ids with SPI-registered ids (unknown config ids dropped with **warn**). **Wildcard:** if the property is **missing**, **`null`**, **empty**, or **any** list element equals **`*`** (trimmed; asterisk only), treat as **all SPI formats** on HTTP. If **`*`** appears together with other ids, **wildcard wins** (all formats on HTTP). |
| *(limits)* | *(optional)* | Row cap / timeout / max duration keys: nest under **`mill.data.services.export`** (e.g. **`…limits`**) or document a sibling prefix in **WI-254** — separate from SPI vs HTTP format visibility. |

**Unknown ids** in **`formats`** that are not registered in SPI: **log warn** at startup (or first request) and **ignore**; do **not** fail context refresh unless you add an explicit **strict** flag later.

**Rejected `?format=`:** return **400** with a clear message if the id is not in the **effective HTTP allowlist** (post-wildcard / intersection rules).

## Well-known discovery (`/.well-known/mill`)

Register export with the same **`Descriptor`** pattern as the HTTP data plane so **[`WellKnownService`](../../../../services/mill-service-common/src/main/java/io/qpointz/mill/service/service/WellKnownService.java)** picks up beans automatically:

1. **`ExportServiceDescriptor`** — `@Component`, `@ConditionalOnService(value = "export", group = "data")`, implements **[`Descriptor`](../../../../services/mill-service-api/src/main/java/io/qpointz/mill/service/descriptors/Descriptor.java)** with **`getTypeName()`** = **`DescriptorTypes.SERVICE_TYPE_NAME`** (`"services"`), and logical **`name`** = **`data-export`** (stable id next to **`data-http`**, **`data-grpc`** in **[`HttpServiceDescriptor`](../../../../services/mill-data-http-service/src/main/java/io/qpointz/mill/data/backend/access/http/configuration/HttpServiceDescriptor.java)** / **[`GrpcServiceDescriptor`](../../../../services/mill-data-grpc-service/src/main/kotlin/io/qpointz/mill/data/backend/grpc/GrpcServiceDescriptor.kt)**). **`@EnableConfigurationProperties(ExportServiceProperties.class)`** on the descriptor class or a dedicated export configuration class is fine if that matches module layout.

2. **`ExportConnectionDescriptor`** — `@Component`, same **`@ConditionalOnService`**, implements **`Descriptor`** with **`getTypeName()`** = **`DescriptorTypes.CONNECTIONS_TYPE_NAME`** (`"connections"`). Mirror **[`HttpConnectionDescriptor`](../../../../services/mill-data-http-service/src/main/java/io/qpointz/mill/data/backend/access/http/configuration/HttpConnectionDescriptor.java)**: resolve **scheme / host / port** from **`ExportServiceProperties#getExternalHost()`** + optional **`ExternalHostsProvider`**, with the same localhost fallbacks when externals are absent. Expose **`api-path`** **`/services/export/`** (JSON property **`api-path`**, `@JsonProperty` like HTTP’s **`/services/jet/`**).

**Optional:** add **effective HTTP format ids** (post-allowlist) as a field on **`ExportServiceDescriptor`** so cold-start clients can skip **`GET /formats`**; if omitted, document that discovery uses **`GET …/formats`** only.

## Scope

1. **`ExportFacility`** (Spring `@Component` or `@Service`): injects **`ExportFormatRegistry`**, **`ExportServiceProperties`** (allowlist / wildcard), and optional **`ExternalHostsProvider`** for catalog URL building; resolves **`?format=`** against the **effective HTTP set**, then encoder via registry; calls **`ExportVectorBlockSource`** for iterator; runs **pump** loop. **Catalog controllers** use the **same effective set** for **`/formats`**, **`/catalog`**, and per-schema URL lists.
2. Spring Web MVC with **`StreamingResponseBody`** (or equivalent) so the **HTTP response** forwards bytes as the format writer flushes—**no** buffering of the full export body in memory.
3. Respect [`ServicesSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ServicesSecurityConfiguration.java) (`/services/**`).
4. Filename sanitization; **Content-Disposition** / **Content-Type** from SPI descriptor.
5. Wire **`mill-service`** + [`settings.gradle.kts`](../../../../settings.gradle.kts); add **`mill.data.services.export`** keys to sample **[`application.yml`](../../../../apps/mill-service/src/main/resources/application.yml)** (`enable`, **`external-host`** aligned with HTTP, **`formats`** as needed).
6. **`ExportServiceProperties`**: resolve **effective HTTP formats** from **`formats`** + registry; build catalog URLs using **`ExternalHostsProvider`** + **`external-host`**.
7. **`ExportServiceDescriptor`** + **`ExportConnectionDescriptor`** for **`/.well-known/mill`** (**Well-known** section above); ensure **`mill-export-service`** depends on **`mill-service-api`** (and existing Mill service BOM) so **`Descriptor`** types resolve.
8. **OpenAPI (SpringDoc):** all export **`@RestController`** classes are documented with **`io.swagger.v3.oas.annotations`** — at minimum **`@Tag`** on the controller, **`@Operation`** on each handler, and **`@Parameter`** / **`@ApiResponse`** (or **`@Content`**) where useful. Follow the same style as [`SchemaExplorerController.kt`](../../../../data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerController.kt) / other **`@Tag`** services. Document **JSON** discovery endpoints with appropriate **`produces`** / response schemas (**`@Schema`**-annotated DTOs or documented `Map` structure in operation descriptions). **Streaming** endpoints (**`POST /sql`**, table **GET**) must declare **`produces`** media types per format (**`?format=`** drives **`Content-Type`** — describe in summary or note **multiple content types**). Controllers under **`/services/export/**`** are included in the existing **`mainApi`** SpringDoc group from [`OpenApiConfiguration`](../../../../services/mill-service-common/src/main/java/io/qpointz/mill/service/configuration/OpenApiConfiguration.java) (no extra `GroupedOpenApi` required unless the team prefers a dedicated **export** group later).

## Acceptance

- Smoke: **`POST /sql`** and **`GET /schemas/{schema}/tables/{table}`** return bytes in requested format for a fixture dataset.
- **`ExportFacility`** covered by unit test with test doubles for vector source + format encoder (or narrow IT).
- Response path aligns with **WI-250** bounded-memory contract (spot-check: no “collect all rows then write” in export orchestration).
- **`POST /sql`** with **`?format=`** rejected (**400**) when format is not in the **effective HTTP allowlist** from **`mill.data.services.export.formats`** (with wildcard rules); accepted when allowlisted even though SPI may register more providers.
- **`GET /catalog`** and **`GET /formats`** list **only** the **effective HTTP format set** (**SPI** filtered by **`mill.data.services.export.formats`** / wildcard rules); **`catalog`** includes **`formats`** at the **root** alongside **`schemas`**.
- With **`mill.data.services.export.enable: true`**, **`GET /.well-known/mill`** response includes an entry under **`services`** with **`name`: `data-export`**, and under **`connections`** an object with **`api-path`** **`/services/export/`** and host resolution consistent with **`mill.data.services.export.external-host`**. With export **disabled** (or service group absent per bootstrap), those beans are absent and do **not** appear in well-known.
- **SpringDoc:** with export enabled, **`/v3/api-docs`** ( **`api`** group per [`OpenApiConfiguration`](../../../../services/mill-service-common/src/main/java/io/qpointz/mill/service/configuration/OpenApiConfiguration.java)) lists all **`/services/export/**`** operations under a **`@Tag`** (e.g. **`export`**) with summaries per handler; spot-check in Swagger UI if used.
- Catalog JSON shape remains documented in OpenAPI operation descriptions or shared **`@Schema`** DTOs where practical.

## Depends on

**WI-250**, **WI-251**, **WI-252** (facility must call vector source, then format encoder).
