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
| **Chat** | `/chat/*` | General conversations |
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
- When the backend binds metadata to that node, **`metadataEntityId`** is present and the UI can load/save facets via the metadata API. If it is missing, facet editing stays disabled.

### Facets

Facet blocks are driven by **facet type** definitions from the server (titles, single vs multiple instances, JSON/schema-driven forms). Standard types (descriptive, structural, relations, value mappings, concepts) appear when **`mill.metadata.seed.resources`** includes the platform bootstrap (for example **`classpath:metadata/platform-bootstrap.yaml`**) or equivalent data was imported; Flyway alone does not insert those definitions.

**MULTIPLE** cardinality facets render as **one card per instance** (e.g. relations), including legacy `{ "relations": [...] }` payloads and a single `{}` row from JPA as one logical entry.

### Search in Model

Use the **header global search** (when enabled): **Ctrl+K** / **Cmd+K** opens search across views, schemas, tables, columns, concepts, and saved queries (`GlobalSearch.tsx`).

### URLs

Deep links look like **`/app/model/<schema>/<table>/<column>`** (omit segments as needed). Feature flag **`viewModel`** must be on.

### Metadata editing

Editing requires a signed-in user (when auth is enabled), **`metadataEntityId`** on the selected entity, and facet types that allow writes. Facet type administration is under **`/admin/model/facet-types`** (sidebar **Metadata** → **Facet types**) when **Admin** and **Facet types** are enabled.

For a fuller metadata model description, see **[Metadata](metadata/index.md)**.

---

## Knowledge view

The sidebar label is **Knowledge** (`/knowledge`). It lists **concepts** with **categories** and **tags** (when those sidebar features are enabled). The detail panel can show description, related entities, SQL definition, and other fields depending on **knowledge\*** feature flags.

URLs: **`/app/knowledge`** or **`/app/knowledge/<conceptId>`**.

---

## Analysis view

**Analysis** (`/analysis`) is the **query playground**: edit SQL, format, copy, clear, execute (when the backend allows), and inspect results — gated by **`analysis*`** flags.

---

## Chat view

### Behaviour (as implemented in `ui/mill-ui`)

- **General Chat** uses **`ChatInputBox`**: a single text area; **Enter** sends the message, **Shift+Enter** inserts a new line.
- There is **no** slash-command palette (`/get-data`, …) and **no** `@` mention picker in the composer; those patterns are **not** implemented in the current Mill UI codebase.
- Optional toolbar actions (when flags are on): **attach** and **dictate** buttons — UI only unless wired to a backend.
- Assistant replies are rendered as **Markdown** (including fenced code blocks and tables) via **`MessageContent`** / **`ReactMarkdown`**.
- A **thinking** line can show while the assistant is streaming.
- Conversations are persisted in **`localStorage`** on the client; creating a chat also calls **`chatService.createChat`** (see `ChatContext.tsx`).

### Chat service (mock vs real)

`src/services/api.ts` re-exports **`chatService`** from **`chatService.ts`**. The checked-in default is a **mock** implementation that streams canned Markdown responses (and varies tone by context for inline chats). To point at a real Mill chat API, replace that export with a real implementation — the UI already expects **`createChat`**, **`sendMessage`** streaming, and related methods on the **`ChatService`** interface.

Do **not** assume NL-to-SQL-specific widgets (intent cards, SQL copy tiles, clarification cards) unless your deployed backend and UI branch actually implement them; the stock Mill UI does not render those as separate components today.

### Inline chat

When **inline chat** flags are on, compact chat can open from **Model**, **Knowledge**, or **Analysis** with context passed into **`createChat`**. That uses the same **`ChatInputBox`** behaviour as general chat.

### URLs

**`/app/chat`** and child routes under **`/chat/*`**.

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
| Section missing from sidebar | **Feature flags** for that view (home, model, knowledge, analysis, chat, connect, admin). |
| Search never opens | **`headerGlobalSearch`** flag; try **Ctrl/Cmd+K**. |

---

## See also

- **[Metadata](metadata/index.md)** — user-oriented metadata model and operator notes.
- Repository UI code: **`ui/mill-ui/src`** (routes in **`App.tsx`**, navigation in **`AppHeader.tsx`**).
