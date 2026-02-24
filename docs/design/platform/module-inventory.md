# Module Inventory

This inventory captures all Gradle subprojects declared in `settings.gradle.kts` (including aggregate parent modules).

Category legend:
- `pure`: no Spring Boot wiring in `src/main`
- `spring-service`: Spring Boot/Security/MVC/autoconfiguration by design
- `spring-ai`: Spring AI domain APIs in core logic; avoid Spring Boot wiring in core
- `aggregate`: parent/coordination project

| Module | Lang | Category | Purpose | Key internal dependencies | Spring presence | publishArtifacts | Test infrastructure |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `:core` | n/a (aggregate) | aggregate | Core aggregate for docs/tasks | `:core:*` | none | n/a | aggregate `test` / `testIT` orchestration |
| `:core:mill-core` | Java | pure | Common protobuf/gRPC and shared primitives | none (foundation module) | none | `true` | JUnit 5 + Mockito |
| `:core:mill-security` | Java | pure | Authorization policies and security abstractions | none (foundation module) | none | `true` | JUnit 5 + Mockito |
| `:core:mill-service-api` | Java | spring-service | Service-level annotations, descriptors, Spring helpers | `:core:mill-core`, `:core:mill-security` | Spring conditional/service APIs | `true` | JUnit 5 + Mockito + Boot test |
| `:core:mill-service-security` | Java | spring-service | Spring Security authentication/wiring | `:core:mill-security`, `:core:mill-service-api` | Spring Security + Boot config | `true` | JUnit 5 + Boot test + Spring Security test |
| `:core:mill-test-kit` | Java | spring-service | Shared Spring test infrastructure | `:core:mill-service-api`, `:core:mill-service-security` | Spring Boot test scaffolding | `true` | JUnit 5 + Boot test |
| `:core:mill-service-starter` | Java | spring-service | Shared service starter wiring and well-known HTTP endpoints | `:core:mill-service-api`, `:core:mill-service-security` | Spring MVC + Security | `true` | JUnit 5 + Boot test + Spring Security test |
| `:metadata` | n/a (aggregate) | aggregate | Metadata aggregate + stable CI target (`:metadata:build`) | `:metadata:*` | none | n/a | aggregate build/test orchestration |
| `:metadata:mill-metadata-core` | Kotlin | pure | Metadata domain, facets, repositories, validation contracts | none (foundation module) | none | `true` | JUnit 5 + Mockito |
| `:metadata:mill-metadata-autoconfigure` | Kotlin | spring-service | Metadata Spring auto-configuration | `:metadata:mill-metadata-core`, `:core:mill-service-api` | Spring Boot autoconfiguration | `true` | JUnit 5 + Boot test |
| `:metadata:mill-metadata-service` | Kotlin | spring-service | Metadata REST controllers and DTO layer | `:metadata:mill-metadata-core`, `:metadata:mill-metadata-autoconfigure` | Spring MVC + Boot | `false` | JUnit 5 + Boot test |
| `:data` | n/a (aggregate) | aggregate | Data aggregate build/test entrypoint | `:data:*` | none | n/a | aggregate `test` / `testIT` orchestration |
| `:data:mill-data-backend-core` | Java | pure | Core backend dispatchers and execution contracts | `:core:mill-core`, `:core:mill-security` | none | `true` | JUnit 5 + Mockito |
| `:data:mill-data-backends` | Java | pure | Backend implementations and adapters | `:data:mill-data-backend-core`, `:data:mill-data-source-calcite` | none | `true` | JUnit 5 + Mockito + testIT suite |
| `:data:mill-data-testkit` | Kotlin | pure | Backend runner/test helpers (calcite/jdbc/flow) | `:data:mill-data-backend-core`, `:data:mill-data-backends` | none | `true` | JUnit 5 + Mockito |
| `:data:mill-data-autoconfigure` | Java | spring-service | Data lane autoconfiguration and wiring | `:core:mill-service-api`, `:core:mill-service-security`, `:data:mill-data-backend-core`, `:data:mill-data-backends` | Spring Boot autoconfiguration | `true` | JUnit 5 + Boot test |
| `:data:services` | n/a (aggregate) | aggregate | Data service aggregate | `:data:services:*` | none | n/a | aggregate testing/docs |
| `:data:services:mill-data-http-service` | Java | spring-service | HTTP service entrypoint for backend access | `:data:mill-data-autoconfigure`, `:data:mill-data-backends` | Spring MVC controllers | `true` | JUnit 5 + Boot test |
| `:data:services:mill-data-grpc-service` | Java | spring-service | gRPC service entrypoint for backend access | `:data:mill-data-autoconfigure`, `:data:mill-data-backends` | Spring Boot + gRPC + Security config | `true` | JUnit 5 + Boot test |
| `:data:mill-data-source-core` | Kotlin | pure | Source descriptor model and abstraction layer | `:core:mill-core` | none | `true` | JUnit 5 + Mockito |
| `:data:mill-data-source-calcite` | Kotlin | pure | Calcite adapter for source model | `:data:mill-data-source-core`, `:core:mill-core` | none | `true` | JUnit 5 + Mockito |
| `:data:formats` | n/a (aggregate) | aggregate | Source-format aggregate | `:data:formats:*` | none | n/a | aggregate only |
| `:data:formats:mill-source-format-text` | Kotlin | pure | Text/CSV/FWF source formats | `:data:mill-data-source-core` | none | `true` | JUnit 5 |
| `:data:formats:mill-source-format-excel` | Kotlin | pure | Excel source format support | `:data:mill-data-source-core` | none | `true` | JUnit 5 |
| `:data:formats:mill-source-format-avro` | Kotlin | pure | Avro source format support | `:data:mill-data-source-core` | none | `true` | JUnit 5 |
| `:data:formats:mill-source-format-parquet` | Kotlin | pure | Parquet source format support | `:data:mill-data-source-core` | none | `true` | JUnit 5 |
| `:ui` | n/a (aggregate) | aggregate | UI aggregate | `:ui:*` | none | n/a | aggregate docs/tasks |
| `:ui:mill-grinder-service` | Kotlin | spring-service | UI-facing service bridge | `:core:mill-service-api`, `:data:mill-data-autoconfigure` | Spring Boot service | `false` | JUnit 5 + Boot test |
| `:ai` | n/a (aggregate) | aggregate | AI aggregate and reports | `:ai:*` | none | n/a | aggregate `test` / `testIT` |
| `:ai:mill-ai-v1-core` | Java | spring-ai | NL2SQL v1 core (legacy; being replaced) | `:core:mill-core`, `:data:*` | Spring AI + **Spring Boot wiring present in core** | `false` | JUnit 5 + Boot test + IT |
| `:ai:mill-ai-v1-nlsql-chat-service` | Java | spring-service | NL2SQL v1 chat service wrapper | `:ai:mill-ai-v1-core` | Spring Boot service + persistence/web | `false` | JUnit 5 + Boot test + IT |
| `:ai:mill-ai-v2` | Kotlin | spring-ai | NL2SQL v2 core runtime | `:core:mill-core`, `:ai:mill-ai-v2-test` (tests) | Spring AI APIs; Spring Boot entrypoint removed from core | `false` | JUnit 5 + Boot test + IT |
| `:ai:mill-ai-v2-test` | Kotlin | spring-ai | AI v2 test kit/scenarios | none (test support) | Spring AI only in test tooling | `false` | JUnit 5 |
| `:clients` | n/a (aggregate) | aggregate | Clients aggregate and quality tasks | `:clients:*` | none | n/a | aggregate `test` / `testIT` |
| `:clients:mill-jdbc-driver` | Java | pure | JDBC driver implementation | `:core:mill-core`, `:data:mill-data-backends`, `:data:services:mill-data-grpc-service` (testIT) | none in main; Boot test in tests | n/a (not declared) | JUnit 5 + Boot test + testIT |
| `:clients:mill-jdbc-shell` | Java | pure | SQLLine-based JDBC shell app | `:clients:mill-jdbc-driver` | none | `false` | JUnit 5 |
| `:apps` | n/a (aggregate) | aggregate | Apps aggregate | `:apps:*` | none | n/a | aggregate `test` / `testIT` |
| `:apps:mill-service` | Java | spring-service | runnable service/demo app | `:core:*`, `:data:*`, `:metadata:*` | Spring Boot application | `false` | JUnit 5 + Boot test |

## WI-010 Notes

- `SecurityProvider` and `NoneSecurityProvider` now live in `:core:mill-security`; consumers were updated accordingly.
- `:data:mill-data-autoconfigure` no longer carries metadata modules as transitive dependencies.
- `:core:mill-core` and `:data:mill-data-backends` no longer apply `spring-dependency-management`.
- `:core:mill-core` no longer uses `boot.starter.test`.
- `:ai:mill-ai-v1-*` is intentionally left with minimal code churn and documented as legacy.
