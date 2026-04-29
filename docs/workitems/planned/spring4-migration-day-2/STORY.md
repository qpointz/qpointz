# Spring Boot 4 — migration day 2 (Boot 4.0.x + AI 2.0.0-M5)

This story is the **execution plan** for the actual BOM bump from Spring Boot **3.5.x** to **4.0.x**
and all required mechanical follow-ups (Spring Framework 7, Spring Security 7, Jackson 3, SpringDoc
3, Spring AI 2.0 milestone) while keeping the repository **fully green**.

**Reference design:** [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md)

## Constraints

- **Full repo green** at the end of the story (implementation branch):
  - `./gradlew build`
  - `./gradlew test`
  - `./gradlew testIT`
- **Allowed:** Spring AI milestone **2.0.0-M5** (per Spring AI release notes).
- **Not in scope here:** pre-migration cleanup (tracked separately under
  `docs/workitems/in-progress/spring4-pre-migration-cleanup/`).

## Work Items

- [ ] WI-201 — Pin Boot 4 upgrade targets and MR gates (`WI-201-boot4-migration-day-2-plan-and-gates.md`)
- [ ] WI-202 — Bump Spring Boot to 4.0.6 + fix immediate build breakage (`WI-202-bump-spring-boot-to-4-0-6.md`)
- [ ] WI-203 — Upgrade Spring AI to 2.0.0-M5 milestone (`WI-203-upgrade-spring-ai-2-0-0-m5.md`)
- [ ] WI-204 — Upgrade SpringDoc OpenAPI to 3.0.3 (`WI-204-upgrade-springdoc-3-0-3.md`)
- [ ] WI-205 — Migrate Jackson 2.x → 3.x (tools.jackson + JsonMapper) (`WI-205-jackson-3-migration.md`)
- [ ] WI-206 — Spring Security 7.0 fixes + test alignment (`WI-206-spring-security-7-upgrade-fixes.md`)
- [ ] WI-207 — Boot 4 starter coordinate renames (`WI-207-boot4-starter-coordinate-renames.md`)
- [ ] WI-208 — gRPC/http services + client re-validation under Boot 4 (`WI-208-transport-and-client-revalidation.md`)
- [ ] WI-209 — Full-repo CI green + doc status updates (`WI-209-full-repo-green-and-docs.md`)

