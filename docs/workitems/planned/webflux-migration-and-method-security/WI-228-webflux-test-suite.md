# WI-228 — WebFlux test suite (WebTestClient + security)

Status: `planned`  
Type: `test`  
Area: `platform`  
Backlog refs: **P-34**, **P-4**

## Goal

Replace **MockMvc**-based tests with **`WebTestClient`** (or equivalent reactive test utilities) for migrated controllers and ensure **security smoke** coverage aligns with **WI-223**/**WI-227**.

## Scope

1. Update test Gradle dependencies: `spring-boot-starter-test` **WebFlux** variants where tests exercise MVC assumptions (per migration plan Phase 5).
2. Migrate tests called out in [`webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md): metadata, jet access, UI filter, base security tests, NlSql chat IT, etc.—plus any additional suites broken by prior WIs.
3. Add minimal **regression** cases for **`@PreAuthorize`** presence effect (e.g. authenticated vs anonymous) where not already covered.

## Acceptance

- `./gradlew test` (and **`testIT`** where those modules define it) passes for touched modules.
- No remaining **MockMvc** usages in modules **fully** migrated to WebFlux in this story (document exceptions in completion notes if any slice must stay servlet temporarily).

## Depends on

**WI-227**
