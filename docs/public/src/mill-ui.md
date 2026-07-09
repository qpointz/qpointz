# Mill UI

**Mill UI** is the React application in the **`ui/mill-ui`** module. It is the browser interface for exploring the data model, curating knowledge (concepts), running ad hoc SQL in **Analysis**, chatting (general and inline), and administration — depending on how your Mill deployment exposes routes and [feature flags](#feature-flags-and-layout).

---

## URLs and hosting

The Vite build uses **`base: '/app/'`**, so in production the SPA is typically mounted under **`/app`** (for example **`/app/model`**, **`/app/chat`**). Your operator may add another reverse-proxy prefix; use the address bar on your deployment as the source of truth.

---

## Feature flags and layout

Which areas appear in the sidebar and header is controlled by **feature flags** (defaults in `ui/mill-ui/src/features/defaults.ts`, often merged from the server at runtime). If something below is missing in your environment, ask your operator whether that view is disabled.

---

## Main navigation (typical defaults)

The primary sidebar entries (see `AppHeader.tsx`) are:

| Label in UI | Route (after `/app`) | Role |
|-------------|----------------------|------|
| **Model** | `/model/:schema?/:table?/:column?` | Physical schema tree + metadata facets |
| **Knowledge** | `/knowledge/:conceptId?` | Business concepts (categories, tags, SQL definitions) |
| **Analysis** | `/analysis/:queryId?` | Query playground (SQL editor, execute, results) |
| **Chat** | `/chat`, `/chat/:conversationId` | General conversations (optional segment is the server chat id for shareable deep links) |
| **Connect** | `/connect/:section?` | Client integration hints (e.g. Python, Java) |
| **Admin** | `/admin/*` | Operators: data sources, policies, services, settings; **Metadata** (e.g. facet types) under the model admin area |

When **Home** is enabled, **Overview** is available at **`/home`**. **Profile** (account, settings, access tokens) is usually **`/profile`** from the user menu in the header, not the main nav list.

---

## Home (Overview)

When enabled, the overview dashboard surfaces high-level counts (schemas, tables, concepts, saved queries) and shortcuts into Model, Knowledge, Analysis, and Chat.

---

## Model view

The **Model** view loads the physical schema from the schema explorer API and, when metadata is configured, **facet** payloads per schema, table, or **column**.

### Tree

- **Schemas** → **Tables** → **Columns** (the UI labels columns as **Column** in places such as global search badges).

### Entity header

- **Type** and **name** (schema, table, or column).
- **Location** — Explorer-style path (e.g. `schema.table.column`).
- When the backend binds metadata to that node, **`metadataEntityId`** is present and the UI can load/save facets via the metadata API. If it is missing, facet editing stays disabled. The value is a full **Mill URN**; for physical catalog objects it is typically typed as **`urn:mill/model/schema:…`**, **`urn:mill/model/table:…`**, or **`urn:mill/model/attribute:…`** (lowercase path segments). The logical model root uses **`urn:mill/model/model:model-entity`**.

### Facets

Facet blocks are driven by **facet type** definitions from the server (titles, single vs multiple instances, JSON/schema-driven forms). Standard types (descriptive, structural, relations, value mappings, concepts) appear when **`mill.metadata.seed.resources`** includes the platform bootstrap (for example **`classpath:metadata/platform-bootstrap.yaml`**) or equivalent data was imported; Flyway alone does not insert those definitions.

**MULTIPLE** cardinality facets render as **one card per instance** (e.g. relations), including legacy `{ "relations": [...] }` payloads and a single `{}` row from JPA as one logical entry.

The same **descriptor-driven read-only field layout** (labels, stereotypes such as hyperlinks and tags, structural read view) is shared with **general chat** when an agent proposes or captures metadata facets — see [Chat view — facet artefacts](#facet-and-schema-capture-artefacts-general-chat).

### Multi-scope reads

Facet data can come from more than one **metadata scope** when the URL declares them, for example after **Open in model** from chat:

- **`?scope=global,chat-<conversationId>`** — declares which scopes are in play (comma-separated slugs).
- Optional **`?readScope=`** — active subset for reads (must be within `scope=`). Omit to use all declared scopes.

Use the **Scopes** control in the entity header to toggle among declared scopes. Chat-captured facets accepted under chat scope appear when that scope is active.

### Tag filter

When facet payloads include **tags** (descriptive facets, concept `concepts[].tags`, and similar), a **Tags** control in the entity header filters which facet cards are shown. This is a local UI filter only; it does not change server data.

### Search in Model

Use the **header global search** (when enabled): **Ctrl+K** / **Cmd+K** opens search across views, schemas, tables, columns, concepts, and saved queries (`GlobalSearch.tsx`).

### URLs

Deep links look like **`/app/model/<schema>/<table>/<column>`** (omit segments as needed). Feature flag **`viewModel`** must be on.

### Export (tables and query results)

When the deployment enables the export service (**`mill.data.services.export`**) and **mill-ui** flags **`modelTableExportEnabled`** (Model) / analysis export controls (Analysis), users can **download** physical tables and ad hoc **query results** in server-offered formats (for example CSV, TSV, JSON — the live list comes from **`GET /services/export/formats`**). Downloads use the same origin as the UI (relative **`/services/export/...`** URLs). See [Platform runtime — HTTP data export](reference/platform-runtime.md#http-data-export-servicesexport) and the internal design [Streaming HTTP export service](../../design/platform/export-service.md).

### Metadata editing

Editing requires a signed-in user (when auth is enabled), **`metadataEntityId`** on the selected entity, and facet types that allow writes. Facet type administration is under **`/admin/model/facet-types`** (sidebar **Metadata** → **Facet types**) when **Admin** and **Facet types** are enabled.

For a fuller metadata model description, see **[Metadata](metadata/index.md)**.

---

## Knowledge view

The sidebar label is **Knowledge** (`/knowledge`). It lists **concepts** with **categories** and **tags** (when those sidebar features are enabled). The detail panel can show description, related entities, SQL definition, and other fields depending on **knowledge\*** feature flags.

URLs: **`/app/knowledge`** or **`/app/knowledge/<conceptId>`**.

---

## Analysis view

**Analysis** (`/analysis`) is the **query playground**: pick or create **saved queries**, edit SQL in a **CodeMirror** editor (dialect-aware highlighting and schema completions when the backend is available), format, copy, clear, execute, and inspect paged results — gated by **`analysis*`** flags.

When **`mill-service`** includes the Analysis modules, the UI loads:

- **Saved-query catalog** — **`GET/POST/PUT/DELETE /api/v1/analysis/queries`** (sidebar list, create, rename, save, delete).
- **SQL dialect** — **`GET /api/v1/analysis/dialect`** (editor mode and identifier quoting).
- **Ad hoc execution** — session routes under **`/api/v1/query/`** when **`mill.data.services.query.enable`** is true: **`POST /api/v1/query`**, paged **`GET /api/v1/query/{executionId}`**, **`DELETE /api/v1/query/{executionId}`**.

When the export service and UI export controls are enabled, the results toolbar can **download** the full SQL result via **`POST /services/export/sql`** (not only the current grid page). See [Model export](#export-tables-and-query-results).

Operator-facing detail: [Analysis saved-query service](../../design/platform/analysis-saved-query-service.md), [Query result execution service](../../design/platform/query-result-execution-service.md).

---

## Chat view

### Behaviour (as implemented in `ui/mill-ui`)

- **General Chat** uses **`ChatInputBox`**: a single text area; **Enter** sends the message, **Shift+Enter** inserts a new line.
- There is **no** slash-command palette (`/get-data`, …) and **no** `@` mention picker in the composer.
- Optional toolbar actions (when flags are on): **attach** and **dictate** buttons — UI only unless wired to a backend.
- A **thinking** line can show while the assistant is streaming.
- **Persistence:** With **`VITE_CHAT_API=mock`** (Vitest and local mock runs), conversations live in **`localStorage`**. When the UI is built for the **unified AI v3** service (**`mill.ai.enabled`** on the Mill stack and the client wired to **`realChatService`** / REST mode), the sidebar and transcript are **server-backed** (`GET`/`POST` **`/api/v1/ai/chats`** and streaming **`POST …/messages`**). `ChatContext` drops **`localStorage`** as the source of truth in that mode.

### Assistant replies and structured artefacts

With the **real** AI v3 chat service and a profile that emits structured artefacts (for example **`data-analysis`**), assistant turns can include **structured parts** in addition to conversational text:

- **Conversational prose** still renders as **Markdown** via **`MessageContent`** / **`ReactMarkdown`**.
- **Generated SQL** and related payloads appear as **chat-native artefact cards** (condensed preview), not as raw SQL copied into the message bubble — when the backend follows the v3 artefact emit contract.
- **General chat** (`/chat`): condensed **SQL / Data** preview with **Run**, **Export**, **Expand**, and **Open in Analysis** when the **`chatSqlExecute`** feature flag is on (default **on** in `defaults.ts`).
- SQL artefacts that include **`visualizations[]`** (chart configs from **`chart-mapping`**) show **Chart**, **Data**, and **SQL** tabs in the same card. **Run** loads a bounded **full** row snapshot for chart rendering; the **Data** tab keeps paged grid inspection. Charts compile semantic encodings to **ECharts** locally — durable artefacts never embed renderer config or row data.
- **Expand** opens the full chat content pane with the same data view family as Analysis (**`QueryDataView`**), paging, and **Back to message**. Chart-enabled cards open on the **Chart** tab when visualizations are present.
- **Run all** (chat toolbar) executes every SQL artefact in the current conversation when **`chatSqlExecute`** is enabled.

#### Facet and schema-capture artefacts (general chat)

When the active **agent profile** emits metadata facets (for example **`metadata-authoring`** facet proposals or **`data-analysis`** mixed SQL + facet turns), **general chat** (`/chat`) shows them in the **same condensed artefact shell** as SQL and query results:

- A bordered **`ChatArtifactCard`** with **Facet** and **JSON** tabs (tab label format **`Facet:<Type>`**, e.g. `Facet:Descriptive`).
- The **Facet** tab uses the **same read-only field presentation** as the **Model** view for that facet type (loaded from the metadata facet-type API). A status badge (**Active** / **Rejected**) and target entity id appear in the panel header.
- The **JSON** tab shows the full structured wire payload (pretty-printed).
- **Accept** and **Reject** on pending facet proposals call **`POST /api/v1/ai/chats/{chatId}/artifacts/{artifactId}/accept|reject`** — Accept locks the proposal; Reject retracts chat-scope facet rows and removes the artefact from replay.

**Unknown** structured parts still fall back to a generic JSON preview card.

Reloading a conversation (**`GET /api/v1/ai/chats/{id}`**) restores structured **`artifacts[]`** per turn (SQL, data, facet proposals, schema captures) so condensed previews **hydrate without re-running the agent**.

Repository design: **[Chat artefact architecture](../../design/ai/chat-artefact-architecture.md)**, **[SQL artifact visualization protocol](../../design/agentic/sql-artifact-visualization-protocol.md)**, **[Chart UI composite](../../design/agentic/charts/chart-ui-composite.md)**.

### Agent profile

Platform profiles (YAML-seeded — see **[Mill AI configuration](../reference/mill-ai-configuration.md)** § Agent profiles):

| Profile | Behaviour in chat |
|---------|-------------------|
| **`schema-exploration`** | Schema + metadata read — no facet capture |
| **`metadata-authoring`** | Facet proposals only (catalog-generic: descriptive, DQ, relation, …) |
| **`data-analysis`** | SQL artefacts **and** facet proposals on mixed utterances |

- **Create:** optional **`profileId`** on **`POST /api/v1/ai/chats`** when **`chatAgentPicker`** is enabled (otherwise server/default preference applies).
- **Mid-chat switch (general chat only):** toolbar profile **`Select`** when the server advertises two or more profiles; persists via **`PATCH /api/v1/ai/chats/{chatId}`**. Prior transcript and artefacts are kept; only subsequent turns use the new profile.

Design: [`metadata-facet-catalog-v3.md`](../../design/agentic/metadata-facet-catalog-v3.md), [`ai-v3-chat-metadata-scope.md`](../../design/agentic/ai-v3-chat-metadata-scope.md).

### Chat service (mock vs real)

`src/services/api.ts` re-exports **`chatService`** from **`chatService.ts`**. The **mock** streams canned Markdown for tests and offline UX. The **real** implementation targets **`/api/v1/ai/chats`** (see **[GENERAL-CHAT-DESIGN](../../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md)** and **[ai-v3-chat-transport-extensions](../../design/agentic/ai-v3-chat-transport-extensions.md)**). Both honour the same **`ChatService`** surface (**`createChat`**, **`sendMessage`** streaming, list/detail/rename/delete).

The **mock** does not exercise structured SQL artefact cards; use a Mill deployment with **`mill.ai.enabled`** and the real chat service to see condensed/expand behaviour.

### Inline chat

When **inline chat** flags are on, compact chat opens from **Model**, **Knowledge**, or **Analysis** with context passed into **`createChat`** on the first message.

- **Analysis copilot** uses backend profile **`analysis-copilot`** (not the General Chat profile picker). Each turn can include ephemeral **`context.values`** (current SQL, dirty state, execution metadata) so the assistant reasons over the live editor.
- The drawer sits beside the host (**resizable split** on Analysis/Model/Knowledge). The composer matches **General Chat** chrome (autosizing text area in the same pane as the transcript).
- **SQL proposals** render as compact **inline artifact strips** with **Apply**, **Apply & Run**, and **Copy**. **Automation mode** (`manual` / `apply` / `run`) is per-session in the drawer header menu.
- **Host binding:** switching saved queries or routes activates that host's contextual session when one exists; the drawer hides when the current host has no session. **UI session indicators do not survive a full page reload** today — transcript may still exist server-side; see follow-up **context relations** work.
- **Inline model / knowledge** use compact facet strips; they do not use the general-chat condensed SQL/Data/Chart cards.

Design: **[INLINE-CHAT-FOUNDATION](../../design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md)**, **[Analysis copilot profile](../../design/agentic/analysis-copilot-profile.md)**.

### URLs

**`/app/chat`** opens general chat; **`/app/chat/<conversationId>`** (server-issued chat UUID) deep-links the same thread for sharing or bookmarks. The router keeps the path in sync when you change conversations.

---

## Connect view

**Connect** exposes integration-oriented content (e.g. Python and Java sections) when **`viewConnect`** and related flags are enabled.

---

## Admin view

Sections under **`/admin`** include **Data Sources**, **Policies**, **Services**, **Settings**, and **Model → Facet types** — each gated by its own admin flag. **Facet types** route pattern: **`/admin/model/facet-types`** and edit pages under **`.../edit`**.

---

## Authentication and profile

When security is enabled, login/register flows and **Profile** (**`/profile`**) hold account details, settings, and personal access tokens depending on **`profile*`** flags.

---

## Interface features

- **Theme** — Light/dark via header control when **`headerThemeSwitcher`** is on (Mantine color scheme).
- **Global search** — Ctrl/Cmd+K when **`headerGlobalSearch`** is on.
- **Collapsible sidebar** — When **`sidebarCollapsible`** is on.
- **Responsive** — Layout adapts to smaller widths; sidebar can collapse.

---

## Keyboard reference (accurate for current composer)

| Key | Where | Action |
|-----|--------|--------|
| **Enter** | Chat textarea | Send message |
| **Shift+Enter** | Chat textarea | New line |
| **Ctrl+K** / **Cmd+K** | Global | Open global search (when enabled) |
| Arrow keys / Enter / Escape | Search dropdown | Navigate and select results (see `GlobalSearch`) |

---

## Server configuration (operators)

Facet and entity APIs require a running metadata service with a configured **`MetadataRepository`** backend. In Spring Boot this is selected with **`mill.metadata.repository.type`**:

- **`jpa`** — relational persistence (`mill-metadata-persistence` on the classpath; typical production and dev profiles in `apps/mill-service`).
- **`file`** — YAML-backed in-process repository; requires **either** non-blank **`mill.metadata.repository.file.path`** **or** non-empty **`mill.metadata.seed.resources`** (see [Operator guide](metadata/operators.md)).
- **`noop`** — explicit no-op. If neither JPA nor file beans supply real repositories, optional consumers may still get no-op fallbacks (empty reads).

Related keys: **`mill.metadata.seed.resources`**, **`mill.metadata.seed.on-failure`**, **`mill.metadata.facet-type-registry.type`**. Authoritative YAML layout: **`metadata-canonical-yaml-spec.md`** under `docs/design/metadata/` in the repository. Overview: [Metadata](metadata/index.md) and [Metadata in Mill](metadata/system.md).

The UI proxies **`/api`**, **`/auth`**, and **`/.well-known`** to the backend in local Vite dev (`vite.config.ts`); production uses the same origin or gateway rules your operator configures.

---

## Troubleshooting

| Symptom | Things to check |
|---------|-----------------|
| 404 on `/model` | You may need the **`/app`** prefix (`/app/model`). |
| Facets missing or read-only | **`mill.metadata.repository.type`**, **`mill.metadata.seed.resources`** (platform bootstrap), auth, **`metadataEntityId`** on the entity. |
| Chat feels like a demo | **`chatService`** may still be the **mock** in `chatService.ts`; confirm wired implementation. |
| Facet capture shows stub card in general chat | Confirm **`mill.ai.enabled`**, real chat service, and a profile that emits structured facets; reload should restore artefacts from **`GET /api/v1/ai/chats/{id}`**. |
| Facet tab empty but JSON tab has data | Metadata facet-type API may be unavailable; UI falls back to generic object/JSON display. Check **`mill.metadata.*`** and bootstrap seeds. |
| Section missing from sidebar | **Feature flags** for that view (home, model, knowledge, analysis, chat, connect, admin). |
| Search never opens | **`headerGlobalSearch`** flag; try **Ctrl/Cmd+K**. |

---

## See also

- **[Metadata](metadata/index.md)** — user-oriented metadata model and operator notes.
- **[Chat artefact architecture](../../design/ai/chat-artefact-architecture.md)** — structured SQL/data/facet presentation, GET replay, expand pane (repository design).
- Repository UI code: **`ui/mill-ui/src`** (routes in **`App.tsx`**, navigation in **`AppHeader.tsx`**).
