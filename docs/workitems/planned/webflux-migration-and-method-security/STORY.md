# WebFlux migration, REST inventory, and method-level security stubs

Deliver a **reactive WebFlux stack** for HTTP services per [`docs/design/platform/webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md), grounded in an **authoritative REST controller inventory**, and add **explicit method-level authorization** on every operation (minimum: stub expressions with `@PreAuthorize`, convention documented) so fine-grained rules can be tightened later without another mechanical pass.

**Related backlog:** traces **[P-1](../../BACKLOG.md)**–**[P-4](../../BACKLOG.md)** and **[P-34](../../BACKLOG.md)** (umbrella row).

**Design references:**

- [`docs/design/platform/webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md)
- [`docs/design/security/REST-CONTROLLERS-INVENTORY.md`](../../../design/security/REST-CONTROLLERS-INVENTORY.md)
- [`docs/design/security/README.md`](../../../design/security/README.md)

## Work Items

- [ ] WI-220 — REST controllers inventory refresh (`WI-220-rest-controllers-inventory-refresh.md`)
- [ ] WI-221 — Core WebFlux dependencies (Phase 0) (`WI-221-core-webflux-dependencies.md`)
- [ ] WI-222 — Service module WebFlux dependencies (Phase 1) (`WI-222-service-webflux-dependencies.md`)
- [ ] WI-223 — Reactive HTTP security (`SecurityWebFilterChain` + method security enablement) (`WI-223-reactive-http-security.md`)
- [ ] WI-224 — Metadata reactive stack and WebFlux controllers (`WI-224-metadata-webflux-stack.md`)
- [ ] WI-225 — Jet HTTP reactive dispatcher and `AccessServiceController` (`WI-225-jet-http-reactive.md`)
- [ ] WI-226 — NlSql chat, application descriptor, UI `WebFilter`, remaining HTTP surfaces (`WI-226-remaining-controllers-and-ui-filter.md`)
- [ ] WI-227 — `@PreAuthorize` stubs on all REST operations (`WI-227-method-security-preauthorize-stubs.md`)
- [ ] WI-228 — WebFlux test suite (`WebTestClient`, security smoke) (`WI-228-webflux-test-suite.md`)
