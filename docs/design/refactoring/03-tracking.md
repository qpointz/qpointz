# Mill Refactoring — Progress Tracker

**Started:** 2026-02-17
**Last updated:** 2026-02-17
**Branch:** `refactor/ai-assisted`

## Overall Progress

| Block | Iterations | Status |
|-------|-----------|--------|
| A — Extract Pure Modules | 1-7 | **Complete** |
| B — Rewire Consumers | 8-13 | **Complete** (done during block A) |
| C — Reorganize Directories | 14-16 | **Complete** |

---

## Iteration Status

| # | Iteration | Branch | Status | Build | Notes |
|---|-----------|--------|--------|-------|-------|
| 1 | Create `core/mill-security` | refactor/ai-assisted | **Done** | PASS | 15 pure files (User.java/UserRepo.java/AuthenticationMethods.java kept in security-core due to Spring deps) |
| 2 | Purify `mill-metadata-core` | refactor/ai-assisted | **Done** | PASS | 3 config files to mill-metadata-autoconfigure |
| 3 | Refactor SecurityDispatcher/Provider | refactor/ai-assisted | **Done** | PASS | GrantedAuthority to Collection\<String\> |
| 4 | Create `data/mill-data-service` | refactor/ai-assisted | **Done** | PASS | 30 pure files from mill-service-core |
| 5 | Create `metadata/mill-metadata-provider` | refactor/ai-assisted | **Done** | PASS | 17 files, stripped Spring annotations |
| 6 | Create `data/mill-data-autoconfigure` | refactor/ai-assisted | **Done** | PASS | 6 from service-core (backends configs stayed due to tight coupling) |
| 7 | Create `core/mill-security-autoconfigure` | refactor/ai-assisted | **Done** | PASS | All Spring security wiring; mill-security-core removed from settings |
| 8-13 | Rewire consumers | refactor/ai-assisted | **Done** | PASS | Done incrementally during iterations 1-7 |
| 14 | Move source/ into data/ | refactor/ai-assisted | **Done** | PASS | mill-source-core→mill-data-source-core, mill-source-calcite→mill-data-source-calcite |
| 15 | Move metadata-service | refactor/ai-assisted | **Deferred** | -- | Kept in services/ for now |
| 16 | Delete empty old modules | refactor/ai-assisted | **Done** | PASS | source/ deleted, mill-security-core removed from build |

---

## Verification Checklist per Iteration

### Iteration 1 — core/mill-security
- [x] Module `core/mill-security` exists with build.gradle.kts
- [x] 15 pure files moved (authorization/policy/*, AuthenticationType, AuthenticationMethodDescriptor, AuthenticationContext)
- [x] PolicyEvaluatorImplTest moved and passing
- [x] `mill-security-core` re-exports via `api(project(":core:mill-security"))`
- [x] `settings.gradle.kts` includes `:core:mill-security`
- [x] `./gradlew build` passes
- [x] Zero Spring imports in `core/mill-security/`
- [x] Jackson bundle added to version catalog

### Iteration 2 — metadata/mill-metadata-autoconfigure
- [x] Module `metadata/mill-metadata-autoconfigure` exists
- [x] 3 config files moved (MetadataRepositoryAutoConfiguration, MetadataCoreConfiguration, MetadataProperties)
- [x] AutoConfiguration.imports moved
- [x] spring.factories deleted
- [x] `MetadataService.java` has no Spring annotations (uses @RequiredArgsConstructor)
- [x] MetadataService bean defined in autoconfigure
- [x] `apps/mill-service` depends on `mill-metadata-autoconfigure`
- [x] `./gradlew build` passes

### Iteration 3 — SecurityDispatcher/SecurityProvider
- [x] `SecurityDispatcher.authorities()` returns `Collection<String>`
- [x] `SecurityProvider.authorities()` returns `Collection<String>`
- [x] `SecurityContextSecurityProvider` converts via `.getAuthority()`
- [x] `GrantedAuthoritiesPolicySelector` uses string authorities
- [x] Tests updated and passing
- [x] `./gradlew build` passes

### Iteration 4 — data/mill-data-service
- [x] Module `data/mill-data-service` exists
- [x] 30 files moved (dispatchers/*, rewriters/*, providers, descriptors)
- [x] `mill-service-core` re-exports via `api()`
- [x] `./gradlew build` passes
- [x] Zero Spring imports in `data/mill-data-service/`

### Iteration 5 — metadata/mill-metadata-provider
- [x] Module `metadata/mill-metadata-provider` exists
- [x] 7 implementation classes stripped of Spring annotations
- [x] `FileRepository` uses `InputStream`/`Path` instead of Spring `Resource`
- [x] 17 files moved
- [x] Bean registrations added to MetadataConfiguration
- [x] `./gradlew build` passes

### Iteration 6 — data/mill-data-autoconfigure
- [x] Module `data/mill-data-autoconfigure` exists
- [x] 6 files from mill-service-core moved (DefaultServiceConfiguration, PolicyConfiguration, DefaultFilterChainConfiguration, SecurityContextSecurityProvider, GrantedAuthoritiesPolicySelector, MetadataConfiguration)
- [x] `apps/mill-service` depends on `mill-data-autoconfigure`
- [x] All test modules updated with data-autoconfigure dependency
- [x] `./gradlew build` passes

### Iteration 7 — core/mill-security-autoconfigure
- [x] Module `core/mill-security-autoconfigure` exists
- [x] All security-core files copied to security-autoconfigure
- [x] All consumers redirected from mill-security-core to mill-security-autoconfigure
- [x] `mill-security-core` removed from settings.gradle.kts
- [x] `./gradlew build` passes

### Iterations 8-13 — Rewire Consumers
- [x] `mill-data-grpc-service` — depends on `mill-data-autoconfigure`
- [x] `mill-data-http-service` — depends on `mill-data-autoconfigure`
- [x] `mill-well-known-service` — depends on `mill-security-autoconfigure` + `mill-data-autoconfigure`
- [x] `mill-ai-v1-core` — depends on `mill-security-autoconfigure` + `mill-data-autoconfigure`
- [x] `mill-ai-v1-nlsql-chat-service` — depends on `mill-security-autoconfigure`
- [x] `mill-test-kit` — depends on `mill-security-autoconfigure`
- [x] `mill-jdbc-driver` tests — depends on `mill-data-autoconfigure`
- [x] `./gradlew build` passes

### Iterations 14-16 — Directory Reorganization
- [x] `source/mill-source-core` moved+renamed to `data/mill-data-source-core`
- [x] `source/mill-source-calcite` moved+renamed to `data/mill-data-source-calcite`
- [x] `source/formats` moved to `data/formats`
- [x] `settings.gradle.kts` updated with new module paths and names
- [x] All referencing build files updated
- [x] Kotlin plugin references updated to `alias(libs.plugins.kotlin)`
- [ ] `mill-metadata-service` moved to `metadata/` (deferred)
- [x] `core/mill-security-core` removed from build
- [x] Empty `source/` directory removed
- [x] `./gradlew build` passes

---

## Issues / Deviations Log

| Date | Iteration | Issue | Resolution |
|------|-----------|-------|------------|
| 2026-02-17 | 1 | `libs.bundles.jackson` not in version catalog | Added jackson bundle to libs.versions.toml |
| 2026-02-17 | 1 | User.java implements Spring UserDetails | Kept User.java, UserRepo.java, AuthenticationMethods.java in security-core (15 files moved instead of 17) |
| 2026-02-17 | 1 | PolicyEvaluatorImplTest used JUnit 4 Assert | Fixed to JUnit 5 Assertions |
| 2026-02-17 | 1 | mill-metadata-core and source format modules lost jackson transitivity | Fixed by adding explicit jackson deps (api) where needed |
| 2026-02-17 | 5 | MetadataProviderImpl depends on SchemaProvider (in mill-data-service) | Added mill-data-service as dependency of mill-metadata-provider |
| 2026-02-17 | 5 | Stripped @Component/@Service annotations broke bean discovery | Added explicit @Bean registrations in MetadataConfiguration |
| 2026-02-17 | 6 | Backend config files tightly coupled with backend code | Kept 3 config files in backends; only moved 6 from service-core |
| 2026-02-17 | 6-7 | Many test modules needed data-autoconfigure for DefaultServiceConfiguration | Added data-autoconfigure test dependency to security-core, backends, http-service, jdbc-driver, well-known-service, ai-v1-core |
| 2026-02-17 | 14 | Kotlin plugin `kotlin("jvm")` not found in data modules | Changed to `alias(libs.plugins.kotlin)` |
| 2026-02-17 | 15 | metadata-service move to metadata/ | Deferred — low risk, can be done separately |

---

## New Module Summary

| Module | Type | Spring-Free? |
|--------|------|-------------|
| `core/mill-security` | Pure domain | **Yes** |
| `core/mill-security-autoconfigure` | Spring wiring | No |
| `data/mill-data-service` | Pure domain | **Yes** |
| `data/mill-data-autoconfigure` | Spring wiring | No |
| `data/mill-data-source-core` | Pure domain (Kotlin) | **Yes** |
| `data/mill-data-source-calcite` | Pure domain (Kotlin) | **Yes** |
| `metadata/mill-metadata-autoconfigure` | Spring wiring | No |
| `metadata/mill-metadata-provider` | Pure domain | **Yes** |
