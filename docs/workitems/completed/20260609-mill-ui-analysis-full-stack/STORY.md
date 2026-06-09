# mill-ui Analysis view (full stack)

Persist **saved queries**, expose the documented **saved-query catalog** **`GET /api/v1/queries`** and **`GET /api/v1/queries/{queryId}`** per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) (POST create/update of saved queries is **out of scope** unless added to that contract first). Refactor **[`queryService.ts`](../../../../ui/mill-ui/src/services/queryService.ts)** to **HTTP only** — all three methods use **`fetch`** with **`credentials: 'include'`** against **`/api/v1/queries/**`** and **`/api/v1/query/**`** (**not** `POST /api/v1/queries/execute`). Analysis SQL editing uses **CodeMirror 6** (`SqlCodeEditor`) with **schema-linked** completions.

**No env toggle** for query transport — **`mill-service`** (or Vite proxy) is required for Analysis; Vitest mocks **`fetch`** at the service boundary.

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

**Editor note:** Reuse the existing **CodeMirror 6** stack ([`SyntaxCodeEditor`](../../../../ui/mill-ui/src/components/common/SyntaxCodeEditor.tsx), `@uiw/react-codemirror`). Add **`@codemirror/lang-sql`** and schema-driven **`@codemirror/autocomplete`** — **no Monaco** (lighter bundle; no Vite workers).

## Work Items

- [x] WI-256 — Saved queries persistence (Flyway + JPA) (`WI-256-analysis-saved-queries-persistence.md`)
- [x] WI-258 — Neutral analysis HTTP module + `mill-service` wiring (`WI-258-analysis-service-module-wiring.md`)
- [x] WI-257 — REST `/api/v1/queries` list + get by id (`WI-257-analysis-queries-rest-api.md`)
- [x] WI-259 — mill-ui HTTP-only `queryService` (catalog + execution) (`WI-259-mill-ui-real-query-service.md`)
- [x] WI-266 — mill-ui Analysis CodeMirror SQL editor + schema hints (`WI-266-mill-ui-analysis-sql-codemirror-editor.md`)
- [x] WI-260 — Integration tests + docs sync (`WI-260-analysis-integration-tests-and-docs.md`)
