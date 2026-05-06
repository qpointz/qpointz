# mill-ui Analysis view (full stack)

Persist **saved queries**, expose **`GET/POST /api/v1/queries`** per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md), and wire **[`queryService.ts`](../../../../ui/mill-ui/src/services/queryService.ts)** to a **`realQueryService`** with an env toggle (pattern from [`isRestChatBackendActive`](../../../../ui/mill-ui/src/services/chatService.ts)). **[`QueryPlayground`](../../../../ui/mill-ui/src/components/queries/QueryPlayground.tsx)** already exists; execution today is **mock-only**.

This story is **independent** of [`streaming-export-service`](../streaming-export-service/STORY.md); optional later integration via that story’s **WI-255**.

**Related backlog:** **[U-13](../../BACKLOG.md)**

**Design references:**

- [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) — Queries (Analysis) domain
- [`DataOperationDispatcher`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java)
- [`ApiSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ApiSecurityConfiguration.java) — `/api/**`

## Work Items

- [ ] WI-256 — Saved queries persistence (Flyway + JPA) (`WI-256-analysis-saved-queries-persistence.md`)
- [ ] WI-258 — Neutral analysis HTTP module + `mill-service` wiring (`WI-258-analysis-service-module-wiring.md`)
- [ ] WI-257 — REST `/api/v1/queries` list, get by id, execute (`WI-257-analysis-queries-rest-api.md`)
- [ ] WI-259 — mill-ui `realQueryService` + VITE toggle (`WI-259-mill-ui-real-query-service.md`)
- [ ] WI-260 — Integration tests + docs sync (`WI-260-analysis-integration-tests-and-docs.md`)
