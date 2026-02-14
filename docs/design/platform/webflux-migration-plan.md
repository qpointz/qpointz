# WebFlux Reactive Migration Plan

## Overview

Migrate 7 Spring MVC controllers and their underlying service/repository layers to reactive WebFlux, using Mono/Flux throughout the full stack. Includes core module dependency updates.

## Scope

Migrate the following controllers to reactive WebFlux with full reactive stack:

| Module | Controller | Endpoints | Current State |
|--------|------------|-----------|---------------|
| `mill-metadata-service` | MetadataController | 7 | Blocking MVC |
| `mill-metadata-service` | FacetController | 2 | Blocking MVC |
| `mill-metadata-service` | SchemaExplorerController | 3 | Blocking MVC |
| `mill-jet-http-service` | AccessServiceController | 6 | Blocking MVC |
| `mill-ai-nlsql-chat-service` | NlSqlChatController | 8 | Partial WebFlux (1 reactive endpoint) |
| `mill-starter-service` | ApplicationDescriptorController | 1 | Blocking MVC |
| `mill-grinder-service` | GrinderUIFilter | N/A (filter) | Servlet Filter |

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

### 1.1 Update [services/mill-metadata-service/build.gradle.kts](services/mill-metadata-service/build.gradle.kts)

```kotlin
// Replace:
implementation(libs.boot.starter.web)
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

// With:
implementation(libs.boot.starter.webflux)
implementation(libs.springdoc.openapi.starter.webflux.api)
```

### 1.2 Update [services/mill-jet-http-service/build.gradle.kts](services/mill-jet-http-service/build.gradle.kts)

Add WebFlux dependency (the module currently inherits `boot-starter-web` from `mill-starter-backends`):

```kotlin
implementation(libs.boot.starter.webflux)
```

### 1.3 Update [core/mill-starter-service/build.gradle.kts](core/mill-starter-service/build.gradle.kts)

```kotlin
// Replace:
api(libs.boot.starter.web)

// With:
api(libs.boot.starter.webflux)
```

### 1.4 Update [services/mill-grinder-service/build.gradle.kts](services/mill-grinder-service/build.gradle.kts)

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

## Phase 4: Controller Migration

### 4.1 MetadataController ([services/mill-metadata-service/.../MetadataController.java](services/mill-metadata-service/src/main/java/io/qpointz/mill/metadata/api/MetadataController.java))

**Before:**

```java
@GetMapping("/entities/{id}")
public ResponseEntity<MetadataEntityDto> getEntityById(@PathVariable String id, ...) {
    return metadataService.findById(id)
        .map(entity -> dtoMapper.toDto(entity, scope))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

**After:**

```java
@GetMapping("/entities/{id}")
public Mono<ResponseEntity<MetadataEntityDto>> getEntityById(@PathVariable String id, ...) {
    return reactiveMetadataService.findById(id)
        .map(entity -> dtoMapper.toDto(entity, scope))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
}

@GetMapping("/entities")
public Flux<MetadataEntityDto> getEntities(@RequestParam(required = false) String type, ...) {
    return reactiveMetadataService.findAll()
        .map(entity -> dtoMapper.toDto(entity, scope));
}
```

### 4.2 FacetController ([services/mill-metadata-service/.../FacetController.java](services/mill-metadata-service/src/main/java/io/qpointz/mill/metadata/api/FacetController.java))

Convert both endpoints to return `Mono<ResponseEntity<FacetDto>>`.

### 4.3 SchemaExplorerController ([services/mill-metadata-service/.../SchemaExplorerController.java](services/mill-metadata-service/src/main/java/io/qpointz/mill/metadata/api/SchemaExplorerController.java))

```java
@GetMapping("/tree")
public Flux<TreeNodeDto> getTree(@RequestParam(required = false) String schema, ...) {
    return reactiveMetadataService.findAll()
        .filter(e -> schema == null || Objects.equals(e.getSchemaName(), schema))
        .collectList()
        .flatMapMany(entities -> Flux.fromIterable(buildTree(entities, scope)));
}

@GetMapping("/search")
public Flux<SearchResultDto> search(@RequestParam("q") String q, ...) {
    return reactiveMetadataService.findAll()
        .filter(e -> type == null || e.getType().name().equalsIgnoreCase(type))
        .filter(e -> matchesQuery(e, q.toLowerCase()))
        .map(e -> toSearchResult(e, scope))
        .sort(Comparator.comparing(SearchResultDto::getName));
}
```

### 4.4 AccessServiceController ([services/mill-jet-http-service/.../AccessServiceController.java](services/mill-jet-http-service/src/main/java/io/qpointz/mill/services/access/http/controllers/AccessServiceController.java))

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

Create `ReactiveMessageHelper` utility class to handle protobuf/JSON conversion reactively.

### 4.5 NlSqlChatController ([ai/mill-ai-nlsql-chat-service/.../NlSqlChatController.java](ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/controllers/NlSqlChatController.java))

```java
@GetMapping("/chats")
public Flux<Chat> listChats() {
    return chatService.listChats();
}

@PostMapping("/chats")
public Mono<Chat> createChat(@RequestBody Chat.CreateChatRequest request) {
    return chatService.createChat(request);
}

@GetMapping("/chats/{chatId}")
public Mono<Chat> getChat(@PathVariable UUID chatId) {
    return chatService.getChat(chatId)
        .switchIfEmpty(Mono.error(MillStatuses.notFound("Chat not found")));
}
```

### 4.6 ApplicationDescriptorController ([core/mill-starter-service/.../ApplicationDescriptorController.java](core/mill-starter-service/src/main/java/io/qpointz/mill/services/controllers/ApplicationDescriptorController.java))

```java
@GetMapping("mill")
public Mono<ApplicationDescriptor> getInfo() {
    return Mono.just(this.applicationDescriptor);
}
```

### 4.7 GrinderUIFilter -> GrinderUIWebFilter ([services/mill-grinder-service/.../GrinderUIFilter.java](services/mill-grinder-service/src/main/java/io/qpointz/mill/services/grinder/filters/GrinderUIFilter.java))

Rewrite the servlet `Filter` as a WebFlux `WebFilter`:

**Before (Servlet Filter):**

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GrinderUIFilter implements Filter {
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
@ConditionalOnService("grinder")
public class GrinderUIWebFilter implements WebFilter {

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

1. **Core modules first** (Phase 0) - `mill-security-core`, `mill-service-core`, `mill-test-kit`
2. `mill-starter-service` (1 simple endpoint, validates infrastructure)
3. `mill-grinder-service` (filter rewrite, no reactive service layer)
4. `mill-metadata-service` (3 controllers, establishes patterns for repository/service)
5. `mill-ai-nlsql-chat-service` (complete partial migration)
6. `mill-jet-http-service` (protobuf handling complexity)

---

## Files to Create

- `ReactiveMetadataRepository.java`
- `ReactiveFileMetadataRepository.java`
- `ReactiveMetadataService.java`
- `ReactiveDataOperationDispatcher.java`
- `ReactiveMessageHelper.java`
- `GrinderUIWebFilter.java` (replaces GrinderUIFilter)

## Files to Modify

**Core Modules (3 build files):**

- `core/mill-security-core/build.gradle.kts`
- `core/mill-service-core/build.gradle.kts`
- `core/mill-test-kit/build.gradle.kts`

**Service Modules (4 build files):**

- `services/mill-metadata-service/build.gradle.kts`
- `services/mill-jet-http-service/build.gradle.kts`
- `services/mill-grinder-service/build.gradle.kts`
- `core/mill-starter-service/build.gradle.kts`

**Controllers (6 files):**

- `MetadataController.java`
- `FacetController.java`
- `SchemaExplorerController.java`
- `AccessServiceController.java`
- `NlSqlChatController.java`
- `ApplicationDescriptorController.java`

**Filters (1 file - delete and replace):**

- `GrinderUIFilter.java` -> `GrinderUIWebFilter.java`

**Service Implementations:**

- `NlSqlChatServiceImpl.java`
- Service implementations that depend on blocking repositories

**Test Files (6+ files):**

- `MetadataControllerTest.java`
- `AccessServiceControllerTest.java`
- `GrinderUIFilterTest.java`
- `HttpServiceBasicSecurityTest.java`
- `BaseSecurityTest.java`
- `NlSqlChatControllerTestIT.java`

---

## Dependency Graph (Indirect MVC Dependencies)

```
mill-metadata-service
  └── mill-service-core (has jakarta.servlet.api)
        └── mill-security-core (has boot-starter-web)
        └── mill-metadata-core

mill-jet-http-service
  └── mill-starter-backends
        └── mill-service-core (has jakarta.servlet.api)
  └── mill-starter-service (has boot-starter-web)
        └── mill-security-core (has boot-starter-web)

mill-grinder-service
  └── mill-service-core (has jakarta.servlet.api)

mill-ai-nlsql-chat-service
  └── (already has boot-starter-webflux)
```

All paths lead back to `mill-security-core` and `mill-service-core` - these must be migrated first.

---

## Tasks

- [ ] **Phase 0**: Update core module dependencies: mill-security-core, mill-service-core, mill-test-kit - replace boot-starter-web and jakarta.servlet.api with webflux equivalents
- [ ] **Phase 1**: Update service build.gradle.kts files: replace boot-starter-web with boot-starter-webflux in metadata-service, jet-http-service, starter-service, and grinder-service
- [ ] **Phase 2**: Create ReactiveMetadataRepository interface and ReactiveFileMetadataRepository wrapper implementation
- [ ] **Phase 3**: Create ReactiveMetadataService, update NlSqlChatService interface, create ReactiveDataOperationDispatcher
- [ ] **Phase 4a**: Migrate all 7 controllers to return Mono/Flux: ApplicationDescriptorController, MetadataController, FacetController, SchemaExplorerController, NlSqlChatController, AccessServiceController
- [ ] **Phase 4b**: Rewrite GrinderUIFilter as WebFilter for WebFlux compatibility
- [ ] **Phase 5**: Update all test suites: replace MockMvc with WebTestClient, update test dependencies
