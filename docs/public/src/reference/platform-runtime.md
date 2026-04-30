# Platform runtime (Mill services)

Mill’s **Spring Boot** applications (for example `apps/mill-service`, metadata and data HTTP services, security auth service, AI v1 chat service) share a single **version catalog** (`libs.versions.toml` in the repository root). The table below reflects the **Spring Boot 4** migration line (archived story [`spring4-migration-day-2`](../../../workitems/completed/20260430-spring4-migration-day-2/STORY.md)). For rationale, phased checklist, and module impact, see the internal design doc [Spring Boot 3.5 to 4.0 migration plan](../../../design/platform/spring4-migration-plan.md).

| Area | Version / policy |
|------|------------------|
| Java | **21** for building and running the full stack (required by **Spring AI 2.0** milestone artifacts). |
| Spring Boot | **4.0.6** |
| Spring Framework / Security | **7.0** (managed by Spring Boot) |
| Jackson | **3.1.x** (`tools.jackson` coordinates; JSON/YAML via `JsonMapper` / `YAMLMapper`) |
| SpringDoc OpenAPI | **3.0.3** (WebMVC and WebFlux starters where used) |
| Spring AI | **2.0.0-M5** (milestone; NL-to-SQL and related modules) |
| gRPC (data plane) | **grpc-java 1.79.x**; production server in `services/mill-data-grpc-service` (no `net.devh` starter) |
| Jakarta EE | **11** baseline with Boot 4 (Servlet **6.1**, etc.) |

## JDBC driver and integration tests

The **JDBC driver** module is not a Spring Boot application, but its **`testIT`** suite may start an embedded **Skymill** gRPC stack (`EmbeddedSkymillGrpcServer`) so tests do not depend on a fixed host/port. That wiring is described in the migration plan’s **WI-208** notes and the design doc above.

## Verifying a checkout

From the repository root (see also [Installation](../installation.md)):

- `./gradlew build` — compile, unit tests, assemble
- `./gradlew testIT` — integration suites (where configured per module)

Record full-repo **`clean build`**, **`test`**, and **`testIT`** results in merge requests for large platform upgrades.
