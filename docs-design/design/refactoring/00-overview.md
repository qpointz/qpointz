# Mill Platform Refactoring — Overview

**Status:** Planning
**Date:** February 2026

## Goals

1. **Extract all Spring wiring** into dedicated auto-configure modules. Domain/logic modules must have **zero Spring dependency** — no `@Configuration`, `@Bean`, `@Component`, `@ConditionalOn*`, `@ConfigurationProperties`, no Spring Security types.

2. **Reorganize into functional business lanes:**
   - **Core** — protocols, proto, types, exceptions, security (shared infrastructure)
   - **Data** — data integration: sources, formats, storage, backends, gRPC/HTTP data services
   - **Metadata** — its own lane: entities, facets, metadata REST service
   - **AI** — NL2SQL, chat service
   - **Clients**, **UI** — unchanged

3. **Services belong to their lane**, not in a separate directory. The gRPC data service is part of the data lane. The metadata REST API is part of the metadata lane.

4. **Each lane has its own auto-configure module** for Spring wiring.

## Documents

| Document | Content |
|----------|---------|
| [00-overview.md](00-overview.md) | This file — goals and document index |
| [01-iterations.md](01-iterations.md) | Detailed 16-iteration execution plan, bottom-up, each step testable |
| [02-file-inventory.md](02-file-inventory.md) | Spring contamination audit — every file classified as pure or Spring-wired |
| [03-tracking.md](03-tracking.md) | Progress tracker — iteration status, verification checklists, issues log |
| [04-dependency-graph.md](04-dependency-graph.md) | Full module dependency graph annotated PURE / SPRING with contamination notes |
| [05-configuration-keys.md](05-configuration-keys.md) | Configuration key inventory — all mill.* keys, custom annotations, metadata JSON coverage |

## Related Design Documents

- [Spring Boot 4 Migration Plan](../platform/spring4-migration-plan.md)
- [WebFlux Migration Plan](../platform/webflux-migration-plan.md)
- [MetadataProvider Refactoring Plan](../metadata/metadata-provider-refactoring-plan.md)
- [Test Module Inventory](../platform/test-module-inventory.md)
- [Codebase Analysis](../platform/CODEBASE_ANALYSIS_CURRENT.md)

## Target Module Structure

```
core/
  mill-core/                  — proto, types, vectors, exceptions (PURE, unchanged)
  mill-security/              — authorization policies, auth abstractions (PURE)
  mill-security-autoconfigure/ — security filter chains, auth methods, conditions (ALL Spring)

data/
  mill-data-service/          — dispatchers, rewriters, service contracts (PURE)
  mill-data-backends/         — JDBC, Calcite execution providers (PURE)
  mill-data-grpc-service/     — gRPC data service
  mill-data-http-service/     — HTTP data service
  mill-data-autoconfigure/    — backend configs, service wiring (ALL Spring)
  mill-data-source-core/      — storage abstraction, source model (PURE, Kotlin)
  mill-data-source-calcite/   — Calcite adapter for sources (PURE, Kotlin)
  formats/                    — text, excel, avro, parquet (PURE, Kotlin)

metadata/
  mill-metadata-core/         — entities, facets, repository (PURE)
  mill-metadata-provider/     — legacy MetadataProvider, model classes (PURE)
  mill-metadata-service/      — metadata REST API controllers
  mill-metadata-autoconfigure/ — metadata wiring (ALL Spring)

ai/                           — AI lane (unchanged for now)
clients/                      — JDBC driver, shell (unchanged)
ui/                           — Grinder UI (unchanged)
apps/
  mill-service/               — assembly point
```
