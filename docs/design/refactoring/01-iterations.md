# Mill Refactoring — Detailed Iteration Plan

**Status:** Planning
**Date:** February 2026

## Current Module Dependency Graph

```
mill-core
  ├── mill-security-core         (depends on mill-core)
  ├── mill-metadata-core         (depends on jackson)
  └── mill-service-core          (depends on mill-core, mill-security-core, mill-metadata-core)
        ├── mill-data-backends   (depends on mill-service-core)
        │     ├── mill-data-grpc-service (depends on mill-data-backends)
        │     └── mill-data-http-service (depends on mill-data-backends)
        ├── mill-well-known-service (depends on mill-service-core, mill-security-core)
        ├── mill-grinder-service   (depends on mill-service-core)
        ├── mill-test-kit          (depends on mill-service-core, mill-security-core)
        ├── mill-ai-v1-core        (depends on mill-service-core)
        └── mill-metadata-service  (depends on mill-metadata-core, mill-service-core)
```

## Strategy

Each iteration follows this pattern:
1. Create new module OR extract classes from existing module
2. Old module re-exports new module via `api(project(...))` so all downstream consumers continue working unchanged
3. Test with `./gradlew build` (or scoped to affected modules)

Later iterations update consumers to depend directly on new modules and remove old ones.

---

## BLOCK A: Extract Pure Modules (Bottom-Up)

### Iteration 1 — Create `core/mill-security`

**Goal:** Extract pure authorization/policy classes from `mill-security-core` into a new Spring-free module.

- [ ] Create `core/mill-security/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill security — authorization policies and auth abstractions"
    publishArtifacts = true
}
dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
```

- [ ] Move 17 PURE files from `core/mill-security-core/src/main/java/` to `core/mill-security/src/main/java/`:

```
io/qpointz/mill/security/authorization/policy/Action.java
io/qpointz/mill/security/authorization/policy/ActionVerb.java
io/qpointz/mill/security/authorization/policy/PolicyAction.java
io/qpointz/mill/security/authorization/policy/PolicyActionDescriptor.java
io/qpointz/mill/security/authorization/policy/PolicyEvaluator.java
io/qpointz/mill/security/authorization/policy/PolicyEvaluatorImpl.java
io/qpointz/mill/security/authorization/policy/PolicyRepository.java
io/qpointz/mill/security/authorization/policy/PolicySelector.java
io/qpointz/mill/security/authorization/policy/actions/ExpressionFilterAction.java
io/qpointz/mill/security/authorization/policy/actions/TableReadAction.java
io/qpointz/mill/security/authorization/policy/repositories/InMemoryPolicyRepository.java
io/qpointz/mill/security/authorization/policy/repositories/PolicyActionDescriptorRepository.java
io/qpointz/mill/security/authentication/AuthenticationType.java
io/qpointz/mill/security/authentication/AuthenticationMethodDescriptor.java
io/qpointz/mill/security/authentication/AuthenticationContext.java
io/qpointz/mill/security/authentication/basic/providers/User.java
io/qpointz/mill/security/authentication/basic/providers/UserRepo.java
```

- [ ] Move corresponding tests from `core/mill-security-core/src/test/` to `core/mill-security/src/test/`
- [ ] Modify `core/mill-security-core/build.gradle.kts` — add `api(project(":core:mill-security"))`
- [ ] Modify `settings.gradle.kts` — add `include(":core:mill-security")`
- [ ] Test: `./gradlew :core:mill-security:build`
- [ ] Test: `./gradlew :core:mill-security-core:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: all 17 moved files have zero Spring imports

---

### Iteration 2 — Purify `mill-metadata-core`

**Goal:** Move Spring config classes out of `mill-metadata-core` into a new `metadata/mill-metadata-autoconfigure`.

- [ ] Create `metadata/mill-metadata-autoconfigure/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill metadata auto-configuration"
    publishArtifacts = true
}
dependencies {
    api(project(":core:mill-metadata-core"))
    implementation(libs.boot.starter)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
```

- [ ] Move 3 config files from `core/mill-metadata-core/src/main/java/` to `metadata/mill-metadata-autoconfigure/src/main/java/`:

```
io/qpointz/mill/metadata/configuration/MetadataRepositoryAutoConfiguration.java
io/qpointz/mill/metadata/configuration/MetadataCoreConfiguration.java
io/qpointz/mill/metadata/configuration/MetadataProperties.java
```

- [ ] Move `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` to autoconfigure module
- [ ] Delete `core/mill-metadata-core/src/main/resources/META-INF/spring.factories` (legacy)
- [ ] Modify `MetadataService.java` — remove `@Service`, `@ConditionalOnBean`, `@Autowired`; becomes plain class
- [ ] Add `MetadataService` bean definition to `MetadataRepositoryAutoConfiguration`
- [ ] Modify `settings.gradle.kts` — add `include(":metadata:mill-metadata-autoconfigure")`
- [ ] Modify `apps/mill-service/build.gradle.kts` — add `implementation(project(":metadata:mill-metadata-autoconfigure"))`
- [ ] Test: `./gradlew :metadata:mill-metadata-autoconfigure:build`
- [ ] Test: `./gradlew :core:mill-metadata-core:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: no `@Configuration` or `@AutoConfiguration` classes remain in `mill-metadata-core`

---

### Iteration 3 — Refactor `SecurityDispatcher` and `SecurityProvider` interfaces

**Goal:** Replace `GrantedAuthority` (Spring Security) with `Collection<String>` in domain interfaces.

- [ ] Modify `SecurityDispatcher.java` — `grantedAuthorities()` becomes `authorities()` returning `Collection<String>`
- [ ] Modify `SecurityProvider.java` — same change
- [ ] Modify `SecurityDispatcherImpl.java` — update to match new interface
- [ ] Modify `SecurityContextSecurityProvider.java` — convert `GrantedAuthority` to `String` via `.getAuthority()`
- [ ] Modify `GrantedAuthoritiesPolicySelector.java` — update to use `authorities()` returning strings
- [ ] Fix `DataOperationDispatcherImpl` — rename `.grantedAuthorities()` to `.authorities()`
- [ ] Fix `TableFacetFactoryImpl` — same
- [ ] Fix `DefaultServiceConfiguration` — same
- [ ] Fix `DefaultFilterChainConfiguration` — same
- [ ] Fix `MillGrpcService` (if it calls grantedAuthorities) — same
- [ ] Fix test: `GrantedAuthoritiesPolicySelectorTest.java`
- [ ] Test: `./gradlew build`
- [ ] Verify: no file in `mill-service-core` imports `GrantedAuthority` except Spring bridge classes

---

### Iteration 4 — Create `data/mill-data-service`

**Goal:** Extract pure data orchestration classes from `mill-service-core`.

- [ ] Create `data/mill-data-service/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill data service — dispatchers, rewriters, service contracts"
    publishArtifacts = true
}
dependencies {
    api(project(":core:mill-core"))
    api(project(":core:mill-security"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.bundles.logging)
}
```

- [ ] Move 30 PURE files from `core/mill-service-core/src/main/java/`:

```
io/qpointz/mill/services/dispatchers/DataOperationDispatcher.java
io/qpointz/mill/services/dispatchers/DataOperationDispatcherImpl.java
io/qpointz/mill/services/dispatchers/PlanDispatcher.java
io/qpointz/mill/services/dispatchers/PlanDispatcherImpl.java
io/qpointz/mill/services/dispatchers/PlanHelper.java
io/qpointz/mill/services/dispatchers/SecurityDispatcher.java
io/qpointz/mill/services/dispatchers/SecurityDispatcherImpl.java
io/qpointz/mill/services/dispatchers/ResultAllocator.java
io/qpointz/mill/services/dispatchers/ResultAllocatorImpl.java
io/qpointz/mill/services/dispatchers/SubstraitDispatcher.java
io/qpointz/mill/services/SecurityProvider.java
io/qpointz/mill/services/PlanRewriter.java
io/qpointz/mill/services/PlanRewriteChain.java
io/qpointz/mill/services/PlanRewriteContext.java
io/qpointz/mill/services/ServiceHandler.java
io/qpointz/mill/services/ServiceHandlerPlanRewriteContext.java
io/qpointz/mill/services/SchemaProvider.java
io/qpointz/mill/services/SqlProvider.java
io/qpointz/mill/services/ExecutionProvider.java
io/qpointz/mill/services/rewriters/AttributeFacet.java
io/qpointz/mill/services/rewriters/RecordFacet.java
io/qpointz/mill/services/rewriters/TableFacet.java
io/qpointz/mill/services/rewriters/TableFacetFactory.java
io/qpointz/mill/services/rewriters/TableFacetFactoryImpl.java
io/qpointz/mill/services/rewriters/TableFacetFactories.java
io/qpointz/mill/services/rewriters/TableFacetPlanRewriter.java
io/qpointz/mill/services/rewriters/TableFacetsCollection.java
io/qpointz/mill/services/rewriters/TableFacetVisitor.java
io/qpointz/mill/services/descriptors/SecurityDescriptor.java
io/qpointz/mill/services/descriptors/ServiceDescriptor.java
```

- [ ] Move corresponding tests
- [ ] Modify `core/mill-service-core/build.gradle.kts` — add `api(project(":data:mill-data-service"))`
- [ ] Modify `settings.gradle.kts` — add `include(":data:mill-data-service")`
- [ ] Test: `./gradlew :data:mill-data-service:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: `data/mill-data-service` has zero Spring imports

---

### Iteration 5 — Create `metadata/mill-metadata-provider`

**Goal:** Extract legacy MetadataProvider and model classes from `mill-service-core`.

- [ ] Create `metadata/mill-metadata-provider/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill metadata provider — legacy metadata interfaces and models"
    publishArtifacts = true
}
dependencies {
    api(project(":core:mill-core"))
    api(project(":core:mill-metadata-core"))
    implementation(libs.bundles.jackson)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.bundles.logging)
}
```

- [ ] Strip Spring annotations from implementation classes before moving:
  - [ ] `MetadataProviderImpl` — remove `@Service`, `@ConditionalOnMissingBean`
  - [ ] `FileAnnotationsRepository` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`
  - [ ] `FileRelationsProvider` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`, `@ConditionalOnMissingBean`
  - [ ] `NoneAnnotationsRepository` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`
  - [ ] `NoneRelationsProvider` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`
  - [ ] `MetadataV2AnniotationsProvider` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`
  - [ ] `MetadataV2RelationsProvider` — remove `@Component`, `@Lazy`, `@ConditionalOnProperty`
  - [ ] `FileRepository` — replace Spring `Resource`/`ResourceLoader` with `java.io.InputStream`

- [ ] Move 17 files from `core/mill-service-core/src/main/java/`:

```
io/qpointz/mill/services/metadata/MetadataProvider.java
io/qpointz/mill/services/metadata/AnnotationsRepository.java
io/qpointz/mill/services/metadata/RelationsProvider.java
io/qpointz/mill/services/metadata/impl/MetadataProviderImpl.java
io/qpointz/mill/services/metadata/model/Model.java
io/qpointz/mill/services/metadata/model/Schema.java
io/qpointz/mill/services/metadata/model/Table.java
io/qpointz/mill/services/metadata/model/Attribute.java
io/qpointz/mill/services/metadata/model/Relation.java
io/qpointz/mill/services/metadata/model/ValueMapping.java
io/qpointz/mill/services/metadata/impl/file/FileRepository.java
io/qpointz/mill/services/metadata/impl/file/FileAnnotationsRepository.java
io/qpointz/mill/services/metadata/impl/file/FileRelationsProvider.java
io/qpointz/mill/services/metadata/impl/NoneAnnotationsRepository.java
io/qpointz/mill/services/metadata/impl/NoneRelationsProvider.java
io/qpointz/mill/services/metadata/impl/v2/MetadataV2AnniotationsProvider.java
io/qpointz/mill/services/metadata/impl/v2/MetadataV2RelationsProvider.java
```

- [ ] Move corresponding tests
- [ ] Modify `core/mill-service-core/build.gradle.kts` — add `api(project(":metadata:mill-metadata-provider"))`
- [ ] Modify `settings.gradle.kts` — add `include(":metadata:mill-metadata-provider")`
- [ ] Test: `./gradlew :metadata:mill-metadata-provider:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: `metadata/mill-metadata-provider` has zero Spring imports

---

### Iteration 6 — Create `data/mill-data-autoconfigure` and purify `mill-data-backends`

**Goal:** Move Spring configuration classes into a data lane autoconfigure module.

- [ ] Create `data/mill-data-autoconfigure/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill data lane auto-configuration"
    publishArtifacts = true
}
dependencies {
    api(project(":data:mill-data-service"))
    api(project(":data:mill-data-backends"))
    api(project(":metadata:mill-metadata-provider"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.security)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.bundles.logging)
}
```

- [ ] Move 3 files from `data/mill-data-backends/`:

```
io/qpointz/mill/services/configuration/BackendConfiguration.java
io/qpointz/mill/services/jdbc/configuration/JdbcCalciteConfiguration.java
io/qpointz/mill/services/calcite/configuration/CalciteServiceConfiguration.java
```

- [ ] Move 6 files from `core/mill-service-core/`:

```
io/qpointz/mill/services/configuration/DefaultServiceConfiguration.java
io/qpointz/mill/services/configuration/PolicyConfiguration.java
io/qpointz/mill/services/configuration/DefaultFilterChainConfiguration.java
io/qpointz/mill/services/SecurityContextSecurityProvider.java
io/qpointz/mill/security/authorization/policy/GrantedAuthoritiesPolicySelector.java
io/qpointz/mill/services/metadata/configuration/MetadataConfiguration.java
```

- [ ] Modify `mill-data-backends/build.gradle.kts` — remove `libs.boot.starter`, `libs.boot.configuration.processor`
- [ ] Modify `mill-data-backends/build.gradle.kts` — change `api(project(":core:mill-service-core"))` to `api(project(":data:mill-data-service"))`
- [ ] Modify `settings.gradle.kts` — add `include(":data:mill-data-autoconfigure")`
- [ ] Modify `apps/mill-service/build.gradle.kts` — add `implementation(project(":data:mill-data-autoconfigure"))`
- [ ] Test: `./gradlew :data:mill-data-autoconfigure:build`
- [ ] Test: `./gradlew :data:mill-data-backends:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: `mill-data-backends` has zero Spring imports

---

### Iteration 7 — Create `core/mill-security-autoconfigure`

**Goal:** Extract ALL Spring Security configuration from `mill-security-core`.

- [ ] Create `core/mill-security-autoconfigure/build.gradle.kts`

```kotlin
plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}
mill {
    description = "Mill security auto-configuration — filter chains, auth methods, conditions"
    publishArtifacts = true
}
dependencies {
    api(project(":core:mill-core"))
    api(project(":core:mill-security"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.security)
    implementation(libs.boot.starter.web)
    implementation(libs.boot.starter.security.oauth2.resource.server)
    implementation(libs.boot.starter.security.oauth2.client)
    implementation(libs.okhttp)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    runtimeOnly(libs.bundles.logging)
}
```

- [ ] Move 26 SPRING files from `core/mill-security-core/`:

```
io/qpointz/mill/security/annotations/ConditionalOnSecurity.java
io/qpointz/mill/security/annotations/OnSecurityEnabledCondition.java
io/qpointz/mill/security/configuration/SecurityConfig.java
io/qpointz/mill/security/configuration/ApiSecurityConfiguration.java
io/qpointz/mill/security/configuration/AppSecurityConfiguration.java
io/qpointz/mill/security/configuration/AuthRoutesSecurityConfiguration.java
io/qpointz/mill/security/configuration/ServicesSecurityConfiguration.java
io/qpointz/mill/security/configuration/SwaggerSecurityConfig.java
io/qpointz/mill/security/configuration/WellKnownSecurityConfiguration.java
io/qpointz/mill/security/configuration/PolicyActionsConfiguration.java
io/qpointz/mill/security/authentication/AuthenticationMethod.java
io/qpointz/mill/security/authentication/AuthenticationMethods.java
io/qpointz/mill/security/authentication/AuthenticationReader.java
io/qpointz/mill/security/authentication/CompositeAuthenticationReader.java
io/qpointz/mill/security/authentication/basic/BasicAuthenticationMethod.java
io/qpointz/mill/security/authentication/basic/BasicAuthenticationReader.java
io/qpointz/mill/security/authentication/basic/PasswordAuthenticationConfiguration.java
io/qpointz/mill/security/authentication/basic/providers/UserRepoAuthenticationProvider.java
io/qpointz/mill/security/authentication/oauth2/OAuth2AuthenticationConfiguration.java
io/qpointz/mill/security/authentication/oauth2/OAuth2ResourceServiceAuthenticationMethod.java
io/qpointz/mill/security/authentication/token/EntraIdAuthenticationConfiguration.java
io/qpointz/mill/security/authentication/token/EntraIdAuthenticationMethod.java
io/qpointz/mill/security/authentication/token/EntraIdTokenAuthenticationProvider.java
io/qpointz/mill/security/authentication/token/EntraIdProfileLoader.java
io/qpointz/mill/security/authentication/token/BearerTokenAuthenticationReader.java
io/qpointz/mill/utils/SpringUtils.java
```

- [ ] Move 3 remaining SPRING files from `core/mill-service-core/`:

```
io/qpointz/mill/services/annotations/ConditionalOnService.java
io/qpointz/mill/services/annotations/OnServiceEnabledCondition.java
io/qpointz/mill/services/descriptors/ApplicationDescriptor.java
```

- [ ] Move `META-INF/additional-spring-configuration-metadata.json` to autoconfigure module
- [ ] Move corresponding tests
- [ ] Modify `settings.gradle.kts` — add `include(":core:mill-security-autoconfigure")`
- [ ] Modify `apps/mill-service/build.gradle.kts` — add `implementation(project(":core:mill-security-autoconfigure"))`
- [ ] Test: `./gradlew :core:mill-security-autoconfigure:build`
- [ ] Test: `./gradlew build`
- [ ] Verify: `mill-security-core` source tree is EMPTY
- [ ] Verify: `mill-service-core` source tree is EMPTY (only re-export deps in build.gradle.kts)

---

## BLOCK B: Rewire Consumers

### Iteration 8 — Verify `mill-data-backends`

- [ ] Verify `mill-data-backends/build.gradle.kts` says `api(project(":data:mill-data-service"))` (done in iteration 6)
- [ ] Test: `./gradlew :data:mill-data-backends:build`

### Iteration 9 — Update `mill-data-grpc-service`

- [ ] Add `implementation(project(":core:mill-security-autoconfigure"))` to `build.gradle.kts`
- [ ] Test: `./gradlew :data:mill-data-grpc-service:build`

### Iteration 10 — Update `mill-data-http-service`

- [ ] Verify transitive dependencies work (no build.gradle.kts change expected)
- [ ] Test: `./gradlew :data:mill-data-http-service:build`

### Iteration 11 — Update `services/mill-metadata-service`

- [ ] Replace `implementation(project(":core:mill-service-core"))` with appropriate new modules
- [ ] Test: `./gradlew :services:mill-metadata-service:build`

### Iteration 12 — Update `mill-ai-v1-core`

- [ ] Replace `api(project(":core:mill-service-core"))` with `api(project(":data:mill-data-service"))` and `api(project(":metadata:mill-metadata-provider"))`
- [ ] Test: `./gradlew :ai:mill-ai-v1-core:build`

### Iteration 13 — Update remaining consumers

- [ ] Update `services/mill-well-known-service` — replace old module references
- [ ] Update `ui/mill-grinder-service` — replace old module references
- [ ] Update `core/mill-test-kit` — replace old module references
- [ ] Test: `./gradlew build`

---

## BLOCK C: Reorganize Directory Structure

### Iteration 14 — Move `source/` into `data/`

- [ ] Move `source/mill-source-core/` to `data/mill-data-source-core/` (rename)
- [ ] Move `source/mill-source-calcite/` to `data/mill-data-source-calcite/` (rename)
- [ ] Move `source/formats/` to `data/formats/`
- [ ] Update `settings.gradle.kts` — change `:source:mill-source-core` to `:data:mill-data-source-core`, `:source:mill-source-calcite` to `:data:mill-data-source-calcite`, `:source:formats:*` to `:data:formats:*`
- [ ] Update all `build.gradle.kts` files referencing `:source:*` to use new `:data:` paths and module names
- [ ] Test: `./gradlew build`

### Iteration 15 — Move `services/mill-metadata-service` into `metadata/`

- [ ] Move `services/mill-metadata-service/` to `metadata/mill-metadata-service/`
- [ ] Update `settings.gradle.kts`
- [ ] Update `apps/mill-service/build.gradle.kts`
- [ ] Test: `./gradlew build`

### Iteration 16 — Delete empty old modules

- [ ] Remove `core/mill-service-core` from `settings.gradle.kts` and delete directory
- [ ] Remove `core/mill-security-core` from `settings.gradle.kts` and delete directory
- [ ] Remove empty `services/` directory (if applicable)
- [ ] Remove empty `source/` directory
- [ ] Test: `./gradlew build`
- [ ] Verify: final module structure matches target

---

## Final State

```
core/
  mill-core/                     (112 files, PURE — unchanged)
  mill-security/                 (17 files, PURE — iteration 1)
  mill-security-autoconfigure/   (~30 files, ALL Spring — iteration 7)

data/
  mill-data-service/             (~29 files, PURE — iteration 4)
  mill-data-backends/            (~21 files, PURE — iteration 6)
  mill-data-grpc-service/        (4 files — unchanged)
  mill-data-http-service/        (3 files — unchanged)
  mill-data-autoconfigure/       (~9 files, ALL Spring — iteration 6)
  mill-data-source-core/         (PURE, Kotlin — moved+renamed iteration 14)
  mill-data-source-calcite/      (PURE, Kotlin — moved+renamed iteration 14)
  formats/                       (PURE, Kotlin — moved iteration 14)

metadata/
  mill-metadata-core/            (~20 files, PURE — iteration 2)
  mill-metadata-provider/        (~17 files, PURE — iteration 5)
  mill-metadata-service/         (~10 files — moved iteration 15)
  mill-metadata-autoconfigure/   (~5 files, ALL Spring — iteration 2)

ai/                              (unchanged for now)
clients/                         (unchanged)
ui/                              (unchanged)
apps/
  mill-service/                  (updated dependencies)
```
