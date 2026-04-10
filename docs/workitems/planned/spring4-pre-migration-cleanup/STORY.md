
# Spring Boot 4 — pre-migration cleanup (Spring 3.5.x)

Complete all **repository hygiene and alignment** work that does **not** require upgrading to Spring
Boot 4 / Spring Framework 7 / Jackson 3 / Spring AI 2. The goal is to shrink risk and diff size on the
day the BOM moves to 4.0.x.

This story implements backlog **P-5** (*Spring Boot 4.0 pre-migration cleanup*) in
[`docs/workitems/BACKLOG.md`](../../BACKLOG.md). It does **not** bump `springBoot` in `libs.versions.toml`.

**Explicitly out of scope here** (separate story / Boot 4 day):

- Spring Boot 4.x, Spring Security 7.x, Spring Framework 7.x
- Jackson 3.0 migration
- Spring AI 2.0 upgrade
- SpringDoc OpenAPI 3.x upgrade (Boot 4–aligned)
- Renaming starters to Boot 4 coordinates (`spring-boot-starter-webmvc`, OAuth2 starter renames) unless
  the current 3.5 BOM already exposes stable aliases and the change is zero-risk on 3.5
- `net.devh` removal from production — tracked as **P-6** / **WI-085** (may already be done on your branch)

**Reference design:** [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md)
— Phase 1 checklist and §LOW/MODERATE issues that are safe on 3.5.x.

## Work Items

- [ ] WI-097 — gRPC catalog and reference-tree hygiene (`WI-097-grpc-catalog-and-misc-reference-hygiene.md`)
- [ ] WI-098 — Jakarta naming and `javax.annotation` catalog (`WI-098-jakarta-and-annotation-api-catalog.md`)
- [ ] WI-099 — Security test coordinate alignment (`WI-099-spring-security-test-bom-alignment.md`)
- [ ] WI-100 — `misc/` non-product trees: catalogs and stale paths (`WI-100-misc-rapids-and-spring3-catalogs.md`)
- [ ] WI-101 — SpringDoc / OpenAPI version consistency (`WI-101-springdoc-catalog-audit.md`)
- [ ] WI-102 — `META-INF` auto-configuration legacy audit (`WI-102-spring-factories-and-auto-config-imports.md`)
- [ ] WI-103 — Boot 4 jump-start inventory (grep-only checklist) (`WI-103-boot4-jump-start-inventory.md`)
- [ ] WI-104 — Refresh `spring4-migration-plan.md` Phase 1 status (`WI-104-update-spring4-migration-plan-phase1.md`)
