# mill-ui Analysis view (full stack)

Persist **saved queries**, expose the documented **saved-query catalog** **`GET /api/v1/queries`** and **`GET /api/v1/queries/{queryId}`** per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) (POST create/update of saved queries is **out of scope** unless added to that contract first). Wire **[`queryService.ts`](../../../../ui/mill-ui/src/services/queryService.ts)** to **`realQueryService`** with an env toggle (pattern from [`isRestChatBackendActive`](../../../../ui/mill-ui/src/services/chatService.ts)) and target **`/api/v1/query/**`** for execution and paging (**not** `POST /api/v1/queries/execute`). Upgrade the Analysis SQL surface to **Monaco** with **schema-linked** hints/completions (**WI-266**).

**[`QueryPlayground`](../../../../ui/mill-ui/src/components/queries/QueryPlayground.tsx)** and **[`QueryEditor`](../../../../ui/mill-ui/src/components/queries/QueryEditor.tsx)** already exist; saved-query reads and execution are **mock-oriented** until this story lands.

**Consumer alignment (closed stories — do not re-implement here):**

- **Result grid / paging / sessions:** [`query-result-execution-service`](../../completed/20260511-query-result-execution-service/STORY.md) — **`/api/v1/query/**`**.
- **File export (CSV/JSON/…):** [`streaming-export-service`](../../completed/20260507-streaming-export-service/STORY.md) — **`/services/export/**`**; optional Analysis download UX remains **WI-255** in that archive.

This story is **independent** of new export engine work; it **consumes** the above HTTP surfaces as documented in **BACKEND-API**.

**Related backlog:** **[U-13](../../BACKLOG.md)**

**Design references:**

- [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) — Queries (Analysis): catalog, **`/api/v1/query/**`**, **`/services/export/**`**
- [`export-service.md`](../../../design/platform/export-service.md) — export HTTP catalogue
- [`query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md) — session + paging contract
- [`DataOperationDispatcher`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java)
- [`ApiSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ApiSecurityConfiguration.java) — `/api/**`
- [`ServicesSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ServicesSecurityConfiguration.java) — `/services/**` (export)

**Editor / dialect note (planning):** [`monaco-sql-languages`](https://github.com/DTStack/monaco-sql-languages) under `src/languages/*` ships **per-dialect** Monarch keyword lists + **dt-sql-parser** parser bindings — not a single generated “dialect definition” file. Mill can start with **stock Monaco SQL + custom completions** from `schemaService`, then optionally adopt **monaco-sql-languages** after **Monaco version compatibility** is verified.

## Work Items

- [ ] WI-256 — Saved queries persistence (Flyway + JPA) (`WI-256-analysis-saved-queries-persistence.md`)
- [ ] WI-258 — Neutral analysis HTTP module + `mill-service` wiring (`WI-258-analysis-service-module-wiring.md`)
- [ ] WI-257 — REST `/api/v1/queries` list + get by id (`WI-257-analysis-queries-rest-api.md`)
- [ ] WI-259 — mill-ui `realQueryService` + VITE toggle (`WI-259-mill-ui-real-query-service.md`)
- [ ] WI-266 — mill-ui Analysis Monaco SQL editor + schema hints (`WI-266-mill-ui-analysis-sql-monaco-editor.md`)
- [ ] WI-260 — Integration tests + docs sync (`WI-260-analysis-integration-tests-and-docs.md`)
