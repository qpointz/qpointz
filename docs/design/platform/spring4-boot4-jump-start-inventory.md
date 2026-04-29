# Spring Boot 4 — jump-start inventory (grep snapshot)

**Work item:** [WI-103](../workitems/in-progress/spring4-pre-migration-cleanup/WI-103-boot4-jump-start-inventory.md)  
**Purpose:** Pre-upgrade checklist for mechanical Boot 4 / Jackson 3 / Security 7 work. **No code changes** — counts and paths only. Regenerate with the commands in each section when preparing the BOM bump.

**Linked from:** [spring4-migration-plan.md — Phase 3](spring4-migration-plan.md)

---

## 1. `ObjectMapper` (`com.fasterxml.jackson.databind`)

### 1.1 Explicit `import com.fasterxml.jackson.databind.ObjectMapper`

**Count:** **39** files (`*.java`, `*.kt`; excludes `build/`, `.gradle/`).

<details>
<summary>File paths (click to expand)</summary>

- `ai/mill-ai-v1-core/src/main/java/io/qpointz/mill/ai/chat/tasks/ChatTask.java`
- `ai/mill-ai-v1-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/model/UserChat.java`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/core/tool/ToolRequestExtensions.kt`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jProtocolExecutor.kt`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt`
- `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/valuemap/DefaultVectorMappingSynchronizer.kt`
- `ai/mill-ai-v3-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/ConversationScenario.kt`
- `ai/mill-ai-v3-test/src/main/kotlin/io/qpointz/mill/ai/test/scenario/json/JsonExpectations.kt`
- `clients/mill-jdbc-driver/src/main/java/io/qpointz/mill/client/HttpMillErrorBodies.java`
- `clients/mill-jdbc-driver/src/testIT/java/io/qpointz/mill/SkymillSharedQuerySetJdbcIT.java`
- `core/mill-core/src/main/java/io/qpointz/mill/utils/JsonUtils.java`
- `core/mill-core/src/main/java/io/qpointz/mill/utils/YamlUtils.java`
- `core/mill-core/src/test/java/io/qpointz/mill/excepions/statuses/MillStatusDetailsTest.java`
- `core/mill-sql/src/main/kotlin/io/qpointz/mill/sql/dialect/DialectLoader.kt`
- `core/mill-test-kit/src/main/java/io/qpointz/mill/test/scenario/Action.java`
- `core/mill-test-kit/src/main/java/io/qpointz/mill/test/scenario/ActionDeserializer.java`
- `core/mill-test-kit/src/main/java/io/qpointz/mill/test/scenario/ActionOutcome.java`
- `core/mill-test-kit/src/main/java/io/qpointz/mill/test/scenario/Scenario.java`
- `core/mill-test-kit/src/main/java/io/qpointz/mill/test/scenario/ScenarioContext.java`
- `core/mill-test-kit/src/test/java/io/qpointz/mill/test/scenario/ActionDeserializerTest.java`
- `core/mill-test-kit/src/test/java/io/qpointz/mill/test/scenario/ScenarioTest.java`
- `data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerService.kt`
- `data/mill-data-schema-service/src/test/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerServiceTest.kt`
- `data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/descriptor/SourceObjectMapper.kt`
- `data/mill-data-source-core/src/test/kotlin/io/qpointz/mill/source/descriptor/DescriptorSerializationTest.kt`
- `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/facet/FacetPayloadFieldJsonSerde.kt`
- `metadata/mill-metadata-core/src/test/kotlin/io/qpointz/mill/metadata/domain/facet/FacetPayloadFieldJsonSerdeTest.kt`
- `metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/service/FacetTypeManagementService.kt`
- `metadata/mill-metadata-service/src/test/kotlin/io/qpointz/mill/metadata/service/FacetTypeManagementServiceTest.kt`
- `security/mill-security/src/main/java/io/qpointz/mill/security/authorization/policy/expression/ExpressionNodeParser.java`
- `security/mill-security/src/main/java/io/qpointz/mill/security/authorization/policy/io/JsonPolicyExporter.java`
- `security/mill-security/src/main/java/io/qpointz/mill/security/authorization/policy/io/JsonPolicyImporter.java`
- `security/mill-security/src/main/java/io/qpointz/mill/security/authorization/policy/io/YamlPolicyExporter.java`
- `security/mill-security/src/main/java/io/qpointz/mill/security/authorization/policy/io/YamlPolicyImporter.java`
- `security/mill-security/src/test/java/io/qpointz/mill/security/authorization/policy/expression/ExpressionNodeTest.java`
- `security/mill-service-security/src/main/java/io/qpointz/mill/security/authentication/basic/providers/UserRepo.java`
- `services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java`

</details>

### 1.2 Any `ObjectMapper` identifier (broader)

**Count:** **43** files with at least one `ObjectMapper` occurrence (includes types like `ObjectMapperProvider` / comments — re-filter during migration).

**Note:** Jackson 3 / Boot 4 follow-up is tracked in the migration plan (Phase 2–3) and backlog **P-7**; see [spring4-migration-plan.md §3](spring4-migration-plan.md#3-jackson-30-migration).

---

## 2. `PropertyMapper` (`org.springframework.boot.context.config.PropertyMapper`)

**Count:** **0** Java/Kotlin source files under the repo root (excluding `build/`, `.gradle/`, `docs/`).

```bash
grep -R --include='*.java' --include='*.kt' 'PropertyMapper' . | grep -v '/build/' | grep -v '/.gradle/'
```

---

## 3. Boot “internal” extension hooks (migration guide hotspots)

Searched for:

- `EnvironmentPostProcessor`
- `BootstrapRegistry`
- `org.springframework.boot.env.EnvironmentPostProcessor`

**Count:** **0** matches in `*.java` / `*.kt` product sources (same exclusions).

**Note:** Public `org.springframework.boot.autoconfigure.*` imports (e.g. `@AutoConfiguration`) are widespread and are **not** listed here; Boot 4 modularization may still require spot-checks per the [Boot 4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide).

---

## 4. Spring Security 7.0 — representative paths (migration plan §9)

High-touch areas for Security 7 re-validation (paths only; not an exhaustive security audit):

| Area | Paths |
|------|--------|
| Autoconfigure filter chains | `security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/` (`ApiSecurityConfiguration`, `AppSecurityConfiguration`, `AuthRoutesSecurityConfiguration`, `ServicesSecurityConfiguration`, `SwaggerSecurityConfig`, `WellKnownSecurityConfiguration`, `SecurityConfig`, …) |
| Service security helpers | `security/mill-service-security/src/main/java/io/qpointz/mill/security/` |
| Core policy / OAuth wiring | `security/mill-security/src/main/java/io/qpointz/mill/security/` |
| Auth HTTP service | `security/mill-security-auth-service/src/main/kotlin/io/qpointz/mill/security/auth/configuration/` |
| gRPC security | `services/mill-data-grpc-service/src/main/kotlin/io/qpointz/mill/data/backend/grpc/GrpcSecurityInterceptor.kt` (+ `…/test/…/GrpcSecurityInterceptorTest.kt`) |

---

## Regeneration cheat-sheet

```bash
# ObjectMapper import list
grep -R --include='*.java' --include='*.kt' -l 'import com\.fasterxml\.jackson\.databind\.ObjectMapper' . \
  | grep -v '/build/' | grep -v '/.gradle/' | sort -u

# PropertyMapper
grep -R --include='*.java' --include='*.kt' 'org\.springframework\.boot\.context\.config\.PropertyMapper' . \
  | grep -v '/build/' | grep -v '/.gradle/'

# EnvironmentPostProcessor / BootstrapRegistry
grep -R --include='*.java' --include='*.kt' -E 'EnvironmentPostProcessor|BootstrapRegistry' . \
  | grep -v '/build/' | grep -v '/.gradle/'
```
