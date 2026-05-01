# WebFlux Reactive Migration Plan

## Overview

Migrate **production REST controllers**, auth endpoints, and the UI **SPA routing filter** from the Servlet / Spring MVC stack to **Spring WebFlux**, using `Mono` / `Flux` through the HTTP boundary (with bounded-elastic or adapter wrappers where the domain stays blocking initially). Update **`mill-security`** configuration for **`SecurityWebFilterChain`**. Includes core module dependency updates.

### REST surface (compact)

Counts = `@RestController` classes and **mapped handler methods** (method + path). *UI* row is **not** a controller.

| Module (Gradle path)                  | Controllers | Endpoints |
| ------------------------------------- | ----------: | --------: |
| `metadata/mill-metadata-service`      |           4 |        24 |
| `data/mill-data-schema-service`       |           1 |        11 |
| `services/mill-data-http-service`     |           1 |         7 |
| `services/mill-service-common`        |           1 |         1 |
| `ai/mill-ai-v1-nlsql-chat-service`    |           1 |         8 |
| `ai/mill-ai-v3-service`               |           2 |        10 |
| `security/mill-security-auth-service` |           2 |         5 |
| `services/mill-ui-service`            |           — |         — |
| **Total (REST)**                      |      **12** |    **66** |

**Other HTTP:** **`services/mill-ui-service`** — 1 servlet **`Filter`** → **`WebFilter`** (no `@RestController`).

**Inventory below** reflects the codebase as analyzed from `@RestController` classes and their request mappings (2026-04). *Out of scope for this table:* `core/mill-test-kit` test doubles (`TestController`), `services/mill-service-common` **test** `ServiceController`. Keep **[`REST-CONTROLLERS-INVENTORY.md`](../security/REST-CONTROLLERS-INVENTORY.md)** aligned with this section (regenerate from code when controllers change).

---

## Scope — REST controllers and endpoints (code inventory)

Totals match **[REST surface (compact)](#rest-surface-compact)** above. Full paths and verb lists follow.

### `metadata/mill-metadata-service`

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `MetadataEntityController` | `/api/v1/metadata/entities` | **GET** `/` · **GET** `/{id}` · **GET** `/{id}/facets/merge-trace` · **GET** `/{id}/facets` · **GET** `/{id}/facets/{typeKey}` · **POST** `/` · **PUT** `/{id}` · **PATCH** `/{id}` · **DELETE** `/{id}` · **POST** `/{id}/facets/{typeKey}` · **PATCH** `/{id}/facets/{typeKey}/{facetUid}` · **DELETE** `/{id}/facets/{typeKey}/{facetUid}` · **DELETE** `/{id}/facets/{typeKey}` · **GET** `/{id}/history` (**14**) |
| `MetadataFacetController` | `/api/v1/metadata/facets` | **GET** `/` · **GET** `/{typeKey}` · **POST** `/` · **PUT** `/{typeKey}` · **DELETE** `/{typeKey}` (**5**) |
| `MetadataImportExportController` | `/api/v1/metadata` | **POST** `/import` · **GET** `/export` (**2**) |
| `MetadataScopeController` | `/api/v1/metadata/scopes` | **GET** `/` · **POST** `/` · **DELETE** `/{scopeSlug}` (**3**) |

*Older plan names `MetadataController` / `FacetController` are obsolete — logic lives in the Kotlin controllers above.*

### `data/mill-data-schema-service`

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `SchemaExplorerController` | `/api/v1/schema` | **GET** `/context` · **GET** `/` · **GET** `/schemas` · **GET** `/tree` · **GET** `/model` · **GET** `/{schemaName}` · **GET** `/schemas/{schemaName}` · **GET** `/{schemaName}/tables/{tableName}` · **GET** `/schemas/{schemaName}/tables/{tableName}` · **GET** `/{schemaName}/tables/{tableName}/columns/{columnName}` · **GET** `/schemas/{schemaName}/tables/{tableName}/columns/{columnName}` (**11**, all GET) |

*Schema explorer is **not** in `mill-metadata-service`.*

### `services/mill-data-http-service` (Jet / data plane HTTP)

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `AccessServiceController` | `/services/jet` | **POST** `/ListSchemas` · **POST** `/Handshake` · **POST** `/GetSchema` · **POST** `/GetDialect` · **POST** `/ParseSql` · **POST** `/SubmitQuery` · **POST** `/FetchQueryResult` (**7**) |

*There is **no** `mill-jet-http-service` module — Jet HTTP is **`mill-data-http-service`**.*

### `services/mill-service-common`

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `ApplicationDescriptorController` | `/.well-known` | **GET** `/mill` (**1**) |

*Lives in **`mill-service-common`**, consumed by composite apps (not only `mill-starter-service`).*

### `ai/mill-ai-v1-nlsql-chat-service` (`@ConditionalOnService("ai-nl2data")`)

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `NlSqlChatController` | `/api/nl2sql` | **GET** `/chats` · **POST** `/chats` · **GET** `/chats/{chatId}` · **PATCH** `/chats/{chatId}` · **DELETE** `/chats/{chatId}` · **GET** `/chats/{chatId}/messages` · **POST** `/chats/{chatId}/messages` · **GET** `/chats/{chatId}/stream` (**8**; stream is **SSE** / `Flux`) |

*Class-level `consumes`/`produces` JSON with per-method overrides for `ALL` / SSE.*

### `ai/mill-ai-v3-service` (`@ConditionalOnAiEnabled`)

| Class | Base path | Operations |
| ----- | --------- | ---------- |
| `AiChatController` | `/api/v1/ai/chats` | **GET** `/` · **POST** `/` · **GET** `/{chatId}` · **PATCH** `/{chatId}` · **DELETE** `/{chatId}` · **GET** `/{chatId}/messages` · **POST** `/{chatId}/messages` (SSE) · **GET** `/context-types/{contextType}/contexts/{contextId}` (**8**) |
| `AiProfileController` | `/api/v1/ai/profiles` | **GET** `/` · **GET** `/{profileId}` (**2**) |

*`AiChatController` uses `Flux<ServerSentEvent<…>>` for **POST …/messages**; list/get endpoints are still blocking (`List` return types).*

### `security/mill-security-auth-service`

| Class | Base path | Operations | Notes |
| ----- | --------- | ---------- | ----- |
| `AuthController` | `/auth` | **GET** `/me` · **PATCH** `/profile` · **POST** `/logout` (**3**) | Uses **`HttpServletRequest`** / session — WebFlux port needs **`WebSession`** (or equivalent) |
| `AuthPublicController` | `/auth/public` | **POST** `/login` · **POST** `/register` (**2**) | Same session / servlet coupling |

### `services/mill-ui-service`

| Component | N/A | Servlet **`MillUiSpaRoutingFilter`** → **`WebFilter`** (SPA path rewrite, redirect `/` → `/app/`, 405 on non-GET SPA routes) |

### `@RestControllerAdvice` (migrate with controllers)

Keep exception mapping behavior: `AccessServiceProblemAdvice`, `GlobalExceptionHandler` (nlsql), `MetadataExceptionHandler`, `SchemaExceptionHandler`, `AiChatExceptionHandler`. Prefer **`@ControllerAdvice`** compatibility with WebFlux reactive return types (`Mono` error bodies) where applicable.

---

## Phase 0: Core Module Dependency Updates (CRITICAL)

These core modules have indirect MVC/Servlet dependencies that must be updated first.

### 0.1 Update [core/mill-security-core/build.gradle.kts](core/mill-security-core/build.gradle.kts)

```kotlin
// Replace:
implementation(libs.boot.starter.web)

// With:
implementation(libs.boot.starter.webflux)
```

Also update test dependencies to use webflux.

### 0.2 Update [core/mill-service-core/build.gradle.kts](core/mill-service-core/build.gradle.kts)

```kotlin
// Remove or replace:
api(libs.jakarta.servlet.api)

// The jakarta.servlet.api is used for HttpServletRequest/Response types.
// For WebFlux, use ServerWebExchange instead.
```

### 0.3 Update [core/mill-test-kit/build.gradle.kts](core/mill-test-kit/build.gradle.kts)

```kotlin
// Replace:
api(libs.boot.starter.web)

// With:
api(libs.boot.starter.webflux)
```

This affects ALL modules that use mill-test-kit for testing.

---

## Phase 1: Service Module Dependency Updates

Apply WebFlux + Springdoc WebFlux (or **`webflux.api`** only where UI is not embedded) to **every module that registers the controllers above**, except where already on WebFlux.

| Module | Gradle path | Current stack (typical) |
| ------ | ------------- | ------------------------ |
| Metadata | [`metadata/mill-metadata-service/build.gradle.kts`](../../../metadata/mill-metadata-service/build.gradle.kts) | `starter-web`, Springdoc WebMVC UI |
| Schema explorer | [`data/mill-data-schema-service/build.gradle.kts`](../../../data/mill-data-schema-service/build.gradle.kts) | `starter-web`, Springdoc WebMVC UI |
| Jet HTTP | [`services/mill-data-http-service/build.gradle.kts`](../../../services/mill-data-http-service/build.gradle.kts) | inherits / tests use `starter-web` via backends |
| Auth | [`security/mill-security-auth-service/build.gradle.kts`](../../../security/mill-security-auth-service/build.gradle.kts) | `starter-web` (session forms) |
| NL-SQL chat | [`ai/mill-ai-v1-nlsql-chat-service/build.gradle.kts`](../../../ai/mill-ai-v1-nlsql-chat-service/build.gradle.kts) | already **`starter-webflux`** — verify no transitive MVC |
| AI v3 | [`ai/mill-ai-v3-service/build.gradle.kts`](../../../ai/mill-ai-v3-service/build.gradle.kts) | already **`starter-webflux`** |

### 1.1 Update [metadata/mill-metadata-service/build.gradle.kts](../../../metadata/mill-metadata-service/build.gradle.kts)

```kotlin
// Replace:
implementation(libs.boot.starter.web)
implementation(libs.springdoc.openapi.starter.webmvc.ui)

// With:
implementation(libs.boot.starter.webflux)
implementation(libs.springdoc.openapi.starter.webflux.api)
```

### 1.2 Update [data/mill-data-schema-service/build.gradle.kts](../../../data/mill-data-schema-service/build.gradle.kts)

Same replacement pattern as metadata: **`starter-web`** → **`starter-webflux`**, Springdoc WebMVC → **WebFlux** artifact.

### 1.3 Update [services/mill-data-http-service/build.gradle.kts](../../../services/mill-data-http-service/build.gradle.kts)

Ensure the composite app uses **WebFlux only** for the Jet listener: add or switch to **`spring-boot-starter-webflux`**, and drop reliance on **`spring-boot-starter-web`** from **`mill-starter-backends`** for this entrypoint (may require coordination in **`mill-starter-backends`** / parent BOM so Jet does not pull both stacks).

### 1.4 Update [security/mill-security-auth-service/build.gradle.kts](../../../security/mill-security-auth-service/build.gradle.kts)

Switch to **`spring-boot-starter-webflux`** **and** replace **`HttpServletRequest`**-based session login with **`WebSession`** / reactive security session APIs (same WI as controller port; dependency change alone is insufficient).

### 1.5 Update [core/mill-starter-service/build.gradle.kts](core/mill-starter-service/build.gradle.kts)

```kotlin
// Replace:
api(libs.boot.starter.web)

// With:
api(libs.boot.starter.webflux)
```

### 1.6 Update [services/mill-ui-service/build.gradle.kts](services/mill-ui-service/build.gradle.kts)

```kotlin
// Replace:
implementation(libs.boot.starter.web)

// With:
implementation(libs.boot.starter.webflux)
```

---

## Phase 2: Repository Layer Migration

### 2.1 Create Reactive Repository Interface

Create [core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/repository/ReactiveMetadataRepository.java](core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/repository/ReactiveMetadataRepository.java):

```java
public interface ReactiveMetadataRepository {
    Mono<Void> save(MetadataEntity entity);
    Mono<MetadataEntity> findById(String id);
    Mono<MetadataEntity> findByLocation(String schema, String table, String attribute);
    Flux<MetadataEntity> findByType(MetadataType type);
    Flux<MetadataEntity> findAll();
    Mono<Void> deleteById(String id);
    Mono<Boolean> existsById(String id);
}
```

### 2.2 Wrap Existing File-Based Implementation

Create adapter in [core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/repository/file/ReactiveFileMetadataRepository.java](core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/repository/file/ReactiveFileMetadataRepository.java):

```java
@Component
@RequiredArgsConstructor
public class ReactiveFileMetadataRepository implements ReactiveMetadataRepository {
    private final MetadataRepository delegate;
    
    @Override
    public Mono<MetadataEntity> findById(String id) {
        return Mono.fromCallable(() -> delegate.findById(id))
            .flatMap(opt -> opt.map(Mono::just).orElse(Mono.empty()))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Flux<MetadataEntity> findAll() {
        return Flux.defer(() -> Flux.fromIterable(delegate.findAll()))
            .subscribeOn(Schedulers.boundedElastic());
    }
    // ... other methods
}
```

---

## Phase 3: Service Layer Migration

### 3.1 Create Reactive MetadataService

Create [core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/service/ReactiveMetadataService.java](core/mill-metadata-core/src/main/java/io/qpointz/mill/metadata/service/ReactiveMetadataService.java):

```java
@Service
public class ReactiveMetadataService {
    private final ReactiveMetadataRepository repository;
    
    public Mono<MetadataEntity> findById(String id) {
        return repository.findById(id);
    }
    
    public Flux<MetadataEntity> findAll() {
        return repository.findAll();
    }
    
    public Flux<MetadataEntity> findRelatedEntities(String entityId, String scope) {
        return findById(entityId)
            .flatMapMany(entity -> findRelatedEntitiesInternal(entity, scope));
    }
    // ... other methods converted to Mono/Flux
}
```

### 3.2 Update NlSqlChatService Interface

Update [ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/services/NlSqlChatService.java](ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/services/NlSqlChatService.java):

```java
public interface NlSqlChatService {
    Flux<Chat> listChats();
    Mono<Chat> createChat(Chat.CreateChatRequest request);
    Mono<Chat> getChat(UUID chatId);
    Mono<Chat> updateChat(UUID chatId, Chat.UpdateChatRequest request);
    Mono<Boolean> deleteChat(UUID chatId);
    Flux<ChatMessage> listChatMessages(UUID chatId);
    Mono<ChatMessage> postChatMessage(UUID chatId, Chat.SendChatMessageRequest request);
    Flux<ServerSentEvent<?>> chatStream(UUID chatId);  // already reactive
}
```

### 3.3 Create Reactive DataOperationDispatcher

Create [core/mill-service-core/src/main/java/io/qpointz/mill/services/dispatchers/ReactiveDataOperationDispatcher.java](core/mill-service-core/src/main/java/io/qpointz/mill/services/dispatchers/ReactiveDataOperationDispatcher.java):

```java
public interface ReactiveDataOperationDispatcher {
    Mono<HandshakeResponse> handshake(HandshakeRequest request);
    Mono<ListSchemasResponse> listSchemas(ListSchemasRequest request);
    Mono<GetSchemaResponse> getSchema(GetSchemaRequest request);
    Mono<ParseSqlResponse> parseSql(ParseSqlRequest request);
    Mono<QueryResultResponse> submitQuery(QueryRequest request);
    Mono<QueryResultResponse> fetchResult(QueryResultRequest request);
    Flux<VectorBlock> execute(QueryRequest request);  // streaming results
}
```

---

## Phase 4: Controller migration (by module)

Illustrative snippets only — **source of truth** is the Kotlin/Java files linked in [Scope](#scope--rest-controllers-and-endpoints-code-inventory). Prefer `Mono` / `Flux` return types; use `Mono<ResponseEntity<…>>` where status codes vary.

### 4.1 `metadata/mill-metadata-service` (Kotlin)

Migrate **[`MetadataEntityController.kt`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt)**, **[`MetadataFacetController.kt`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataFacetController.kt)**, **[`MetadataImportExportController.kt`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataImportExportController.kt)**, **[`MetadataScopeController.kt`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataScopeController.kt)** to WebFlux return types and reactive services (**Phase 2–3**).

Example pattern for optional bodies (entities):

```java
@GetMapping("/{id}")
public Mono<ResponseEntity<MetadataEntityDto>> getEntityById(@PathVariable String id, ...) {
    return reactiveMetadataService.findById(id)
        .map(entity -> dtoMapper.toDto(entity, scope))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

### 4.2 `data/mill-data-schema-service` — [`SchemaExplorerController.kt`](../../../data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerController.kt)

Eleven **GET** operations under `/api/v1/schema` — return `Mono` / `Flux` from a reactive [`SchemaExplorerService`](../../../data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerService.kt) (or wrap blocking calls with `Mono.fromCallable(…).subscribeOn(Schedulers.boundedElastic())` until the backend is reactive).

### 4.3 `services/mill-data-http-service` — [`AccessServiceController.java`](../../../services/mill-data-http-service/src/main/java/io/qpointz/mill/data/backend/access/http/controllers/AccessServiceController.java)

Seven **POST** operations under `/services/jet` — use **`ReactiveMessageHelper`** + **`ReactiveDataOperationDispatcher`** (Phase 3.3):

```java
@PostMapping("/ListSchemas")
public Mono<ResponseEntity<?>> listSchemas(@RequestBody(required = false) byte[] payload, ...) {
    return ReactiveMessageHelper.apply(
        this.ensurePayload(payload),
        ReactiveMessageHelper::listSchemasRequest,
        reactiveDispatcher::listSchemas,
        ListSchemasRequest.newBuilder(),
        contentTypeHeader,
        acceptsHeader);
}
```

### 4.4 `security/mill-security-auth-service` — [`AuthController.kt`](../../../security/mill-security-auth-service/src/main/kotlin/io/qpointz/mill/security/auth/controllers/AuthController.kt), [`AuthPublicController.kt`](../../../security/mill-security-auth-service/src/main/kotlin/io/qpointz/mill/security/auth/controllers/AuthPublicController.kt)

Replace **`HttpServletRequest`** session usage with **`WebSession`** and Spring Security reactive APIs for programmatic login / logout. Expect this to be one of the **highest-effort** ports in the story.

### 4.5 `ai/mill-ai-v3-service` — [`AiChatController.kt`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt), [`AiProfileController.kt`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiProfileController.kt)

Module already depends on **`spring-boot-starter-webflux`**. Complete migration: change remaining **`List`** / blocking returns to **`Flux`** / **`Mono`** and align `ChatService` behind reactive types.

### 4.6 `ai/mill-ai-v1-nlsql-chat-service` — [`NlSqlChatController.java`](../../../ai/mill-ai-v1-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/controllers/NlSqlChatController.java)

Finish reactive **`NlSqlChatService`** + controller (**Phase 3.2**); base path **`/api/nl2sql`**.

```java
@GetMapping("/chats")
public Flux<Chat> listChats() {
    return chatService.listChats();
}

@GetMapping("/chats/{chatId}")
public Mono<Chat> getChat(@PathVariable UUID chatId) {
    return chatService.getChat(chatId)
        .switchIfEmpty(Mono.error(MillStatuses.notFound("Chat not found")));
}
```

### 4.7 `services/mill-service-common` — [`ApplicationDescriptorController.java`](../../../services/mill-service-common/src/main/java/io/qpointz/mill/service/controllers/ApplicationDescriptorController.java)

**Current (Servlet MVC):** returns a `Map<String, ?>` built by `WellKnownService` (not a single `ApplicationDescriptor` DTO).

```java
@GetMapping({"mill", "mill/", "", "/"})
public Map<String, ?> getInfo() {
    return this.service.metaInfo();
}
```

**Target (WebFlux):** return `Mono<Map<String, ?>>` (or a typed record) using the same service contract.

### 4.8 `services/mill-ui-service` — [`MillUiSpaRoutingFilter.java`](../../../services/mill-ui-service/src/main/java/io/qpointz/mill/ui/MillUiSpaRoutingFilter.java) → **`MillUiWebFilter`**

Rewrite the servlet `Filter` as a WebFlux `WebFilter` (see snippet below).

**Before (Servlet Filter):**

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MillUiSpaRoutingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        val req = (HttpServletRequest)servletRequest;
        val res = (HttpServletResponse)servletResponse;
        val requestURI = req.getRequestURI();
        
        if (ROOT_REQUEST_PATTERN.test(requestURI)) {
            res.sendRedirect("/app/");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
```

**After (WebFlux WebFilter):**

```java
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
@ConditionalOnProperty(name = "mill.ui.enabled", havingValue = "true", matchIfMissing = true)
public class MillUiWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestURI = exchange.getRequest().getURI().getPath();
        
        // Redirect root to /app/
        if (requestURI == null || requestURI.isEmpty() || ROOT_REQUEST_PATTERN.test(requestURI)) {
            return redirectTo(exchange, "/app/");
        }
        
        // Non-APP or static resource - pass through
        if (!ANY_APP_PREDICATE.test(requestURI) || APP_STATIC_RESOURCE_PATTERN.test(requestURI)) {
            return chain.filter(exchange);
        }
        
        // Non-GET to SPA path - 405
        if (ANY_APP_PREDICATE.test(requestURI) && 
            !HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
            exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            exchange.getResponse().getHeaders().add("Allow", "GET");
            return exchange.getResponse().setComplete();
        }
        
        // Forward to /app/index.html (rewrite URI)
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .path("/app/index.html")
            .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
    
    private Mono<Void> redirectTo(ServerWebExchange exchange, String location) {
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(location));
        return exchange.getResponse().setComplete();
    }
}
```

Key differences:

- `Filter` -> `WebFilter`
- `FilterChain.doFilter()` -> `WebFilterChain.filter()` (returns `Mono<Void>`)
- `HttpServletRequest/Response` -> `ServerWebExchange`
- `sendRedirect()` -> set status + Location header
- `RequestDispatcher.forward()` -> mutate request path

---

## Phase 5: Testing Updates

### 5.1 Replace MockMvc with WebTestClient

**Before:**

```java
@Autowired MockMvc mockMvc;

mockMvc.perform(get("/api/metadata/v1/entities/{id}", "test"))
    .andExpect(status().isOk());
```

**After:**

```java
@Autowired WebTestClient webTestClient;

webTestClient.get()
    .uri("/api/metadata/v1/entities/{id}", "test")
    .exchange()
    .expectStatus().isOk();
```

### 5.2 Update Test Dependencies

In all test suites, replace:

```kotlin
implementation(libs.boot.starter.web)
```

With:

```kotlin
implementation(libs.boot.starter.webflux)
```

---

## Key Patterns

### Error Handling

```java
// Use switchIfEmpty for 404s
return service.findById(id)
    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
    .map(ResponseEntity::ok);
```

### Blocking Code Wrapper

```java
// For any remaining blocking calls
Mono.fromCallable(() -> blockingOperation())
    .subscribeOn(Schedulers.boundedElastic())
```

### Streaming Collections

```java
// Return Flux directly for list endpoints (enables streaming)
@GetMapping("/entities")
public Flux<MetadataEntityDto> getEntities() { ... }
```

---

## Migration Order

1. **Core modules first** (Phase 0) — `mill-security-core`, `mill-service-core`, `mill-test-kit`; add **`SecurityWebFilterChain`** port early if any integration app must stay runnable.
2. **`mill-service-common` + `mill-starter-service`** — `ApplicationDescriptorController` (smoke test for well-known on WebFlux).
3. **`mill-ui-service`** — `MillUiWebFilter` (no domain layer).
4. **`mill-metadata-service`** — four Kotlin controllers + reactive metadata repository/service (largest CRUD surface).
5. **`mill-data-schema-service`** — `SchemaExplorerController` (read-only GETs; may wrap blocking `SchemaExplorerService` first).
6. **`mill-ai-v3-service`** — finish reactive return types on chat/profile (Gradle already WebFlux).
7. **`mill-ai-v1-nlsql-chat-service`** — complete service + controller reactive parity.
8. **`mill-data-http-service`** — Jet protobuf/JSON (`AccessServiceController`) + reactive dispatcher/helper.
9. **`mill-security-auth-service`** — session-based auth last or parallel track ( servlet → `WebSession` ).

---

## Files to Create

- `ReactiveMetadataRepository.java`
- `ReactiveFileMetadataRepository.java`
- `ReactiveMetadataService.java`
- `ReactiveDataOperationDispatcher.java`
- `ReactiveMessageHelper.java`
- `MillUiWebFilter.java` (replaces MillUiSpaRoutingFilter)

## Files to Modify

**Core Modules (3 build files):**

- `core/mill-security-core/build.gradle.kts`
- `core/mill-service-core/build.gradle.kts`
- `core/mill-test-kit/build.gradle.kts`

**Security / HTTP (`SecurityWebFilterChain` port):**

- `security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/*.java` (replace `SecurityFilterChain` beans with reactive equivalents)

**Service / feature modules (Gradle `build.gradle.kts`):**

- `metadata/mill-metadata-service`
- `data/mill-data-schema-service`
- `services/mill-data-http-service`
- `security/mill-security-auth-service`
- `services/mill-ui-service`
- `core/mill-starter-service`

**Controllers & handlers (production):**

- `metadata/mill-metadata-service/.../MetadataEntityController.kt`
- `metadata/mill-metadata-service/.../MetadataFacetController.kt`
- `metadata/mill-metadata-service/.../MetadataImportExportController.kt`
- `metadata/mill-metadata-service/.../MetadataScopeController.kt`
- `data/mill-data-schema-service/.../SchemaExplorerController.kt`
- `services/mill-data-http-service/.../AccessServiceController.java`
- `services/mill-service-common/.../ApplicationDescriptorController.java`
- `ai/mill-ai-v1-nlsql-chat-service/.../NlSqlChatController.java`
- `ai/mill-ai-v3-service/.../AiChatController.kt`
- `ai/mill-ai-v3-service/.../AiProfileController.kt`
- `security/mill-security-auth-service/.../AuthController.kt`
- `security/mill-security-auth-service/.../AuthPublicController.kt`

**`@RestControllerAdvice` (co-migrate):**

- `metadata/mill-metadata-service/.../MetadataExceptionHandler.kt`
- `data/mill-data-schema-service/.../SchemaExceptionHandler.kt`
- `services/mill-data-http-service/.../AccessServiceProblemAdvice.java`
- `ai/mill-ai-v1-nlsql-chat-service/.../GlobalExceptionHandler.java`
- `ai/mill-ai-v3-service/.../AiChatExceptionHandler.kt`

**Filters:**

- `services/mill-ui-service/.../MillUiSpaRoutingFilter.java` → `MillUiWebFilter.java`

**Service implementations:**

- `NlSqlChatService` / implementation, `SchemaExplorerService`, metadata services, **`DataOperationDispatcher`** implementations, auth session helpers

**Test suites (non-exhaustive):**

- Metadata / schema / Jet / UI / security / NLSQL / AI v3 controller tests — prefer **`WebTestClient`** after migration

---

## Dependency Graph (Indirect MVC Dependencies)

```
mill-metadata-service (metadata/mill-metadata-service)
  └── mill-service-core (jakarta.servlet until removed)
        └── mill-security-core (spring-boot-starter-web until Phase 0)
        └── mill-metadata-core

mill-data-schema-service
  └── (same transitive roots via starters / shared libs)

mill-data-http-service
  └── mill-starter-backends / mill-starter-service
        └── mill-service-core
        └── mill-starter-service → mill-security-core

mill-security-auth-service
  └── spring-boot-starter-web (until Phase 1.4)

mill-ui-service
  └── spring-boot-starter-web (servlet stack until WebFlux migration)

mill-ai-nlsql-chat-service / mill-ai-v3-service
  └── spring-boot-starter-webflux (verify no transitive MVC)
```

All paths eventually depend on **`mill-security-core`** and **`mill-service-core`** for HTTP security — migrate **Phase 0** before shipping reactive apps.

---

## Tasks

- [ ] **Phase 0**: Update core module dependencies: `mill-security-core`, `mill-service-core`, `mill-test-kit` — WebFlux starters; drop or replace `jakarta.servlet-api` usages that block WebFlux.
- [ ] **Phase 1**: Update **`build.gradle.kts`** for `mill-metadata-service`, `mill-data-schema-service`, `mill-data-http-service`, `mill-security-auth-service`, `mill-ui-service`, `mill-starter-service` — WebFlux + Springdoc WebFlux as needed; resolve dual MVC+WebFlux on classpath.
- [ ] **Phase 1b** (security): Port servlet **`SecurityFilterChain`** beans in `mill-security-autoconfigure` to **`SecurityWebFilterChain`**; enable reactive method security if using `@PreAuthorize`.
- [ ] **Phase 2**: `ReactiveMetadataRepository` + file-backed adapter in **`mill-metadata-core`**.
- [ ] **Phase 3**: `ReactiveMetadataService` (or Kotlin equivalent), reactive `NlSqlChatService`, `ReactiveDataOperationDispatcher` + Jet `ReactiveMessageHelper`.
- [ ] **Phase 4**: Migrate **12** `@RestController` classes (**66** operations) + **`@RestControllerAdvice`** peers + **`MillUiWebFilter`**; highest risk: **`AuthController`** / **`AuthPublicController`** session port.
- [ ] **Phase 5**: Replace **MockMvc** with **`WebTestClient`** (and reactive security tests) for all touched modules.
