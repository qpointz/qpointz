# Backend API Requirements -- Gap Analysis and Backlog

## Goal

Standalone backend planning document that:
- Lists every data contract the UI requires (formalized from code analysis)
- Maps each requirement against existing backend endpoints
- Identifies gaps, shape mismatches, and missing capabilities
- Provides a prioritized backend development backlog

---

## Part 1: Formalized Backend Requirements (derived from UI code)

Every piece of data the UI consumes, organized by domain. Each entry includes the data shape the UI expects (from `src/types/*.ts`) and where in the UI it is consumed.

### Domain 1: Schema / Data Model

**Consumers:** OverviewDashboard (stats), DataModelLayout, SchemaTree, EntityDetails, facet components

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Full schema tree | `SchemaEntity[]` -- recursive `{id, type, name, children?}` where type is `SCHEMA/TABLE/ATTRIBUTE` | SchemaTree sidebar, OverviewDashboard (schema/table counts) |
| Single entity lookup by ID | `SchemaEntity` -- `{id, type, name}` | DataModelLayout (URL param resolution) |
| All facets for an entity | `EntityFacets` -- `{descriptive?, structural?, relations?}` in a single response | EntityDetails tabbed panel |

**EntityFacets detail:**

- `DescriptiveFacet`: `{displayName?, description?, businessMeaning?, businessDomain?, businessOwner?, tags?, synonyms?}`
- `StructuralFacet`: `{physicalName?, physicalType?, precision?, scale?, isPrimaryKey?, isForeignKey?, isUnique?, nullable?, defaultValue?}`
- `RelationFacet[]`: `{id, name, sourceEntity, targetEntity, cardinality: '1:1'|'1:N'|'N:1'|'N:N', relationType: 'FOREIGN_KEY'|'LOGICAL'|'HIERARCHICAL', description?}`

### Domain 2: Concepts / Knowledge

**Consumers:** OverviewDashboard (concept count), ContextLayout, ContextSidebar, ConceptDetails

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| List all concepts (with optional filter) | `Concept[]` filtered by `?category=X` or `?tag=X` | ContextSidebar concept list |
| Single concept by ID | `Concept` | ConceptDetails panel |
| Category list with counts | `{name: string, count: number}[]` | ContextSidebar category filter |
| Tag list with counts | `{name: string, count: number}[]` | ContextSidebar tag filter |

**Concept shape:** `{id, name, category, tags: string[], description, sql?, relatedEntities?: string[], source?: 'MANUAL'|'INFERRED'|'IMPORTED', createdAt?, updatedAt?}`

### Domain 3: Queries / Analysis

**Consumers:** OverviewDashboard (query count), QueryPlayground, QuerySidebar, QueryEditor, QueryResults

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| List saved queries | `SavedQuery[]` | QuerySidebar list |
| Single saved query by ID | `SavedQuery` | QueryEditor (load from URL) |
| Execute SQL | Request: `{sql: string}` -> Response: `QueryResult` | QueryPlayground execute button |

**SavedQuery shape:** `{id, name, description?, sql, createdAt, updatedAt, tags?: string[]}`

**QueryResult shape:** `{columns: {name, type}[], rows: Record<string, string|number|boolean|null>[], rowCount, executionTimeMs}`

### Domain 4: Chat / Conversations

**Consumers:** ChatView (AppShell, Sidebar, ChatArea, MessageList, MessageBubble, MessageInput)

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| List all conversations | `Conversation[]` with nested `Message[]` | Sidebar conversation list, loaded on mount |
| Create conversation | Request: `{title}` -> Response: `Conversation` | New chat button |
| Delete conversation | By ID, returns 204 | Delete button with confirmation |
| Send message (streaming) | Request: `{content}` -> SSE stream of text chunks | ChatArea message send |

**Conversation shape:** `{id, title, createdAt, updatedAt, messages: Message[]}`

**Message shape:** `{id, conversationId, role: 'user'|'assistant', content, timestamp}`

**SSE format:** `data: {"chunk": "text"}` per event, terminated by `data: [DONE]`

### Domain 5: Inline Chat (Context-Aware)

**Consumers:** InlineChatDrawer, InlineChatPanel, InlineChatInput

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Send context-aware message (streaming) | Request: `{contextType: 'model'|'knowledge'|'analysis', contextId: string, message: string}` -> SSE stream of text chunks | InlineChatPanel |

**Key behavior:** Stateless per request -- context is passed each time. Backend uses context to tailor responses (schema metadata for model, concept definitions for knowledge, query optimization for analysis).

### Domain 6: Chat References

**Consumers:** DataModelLayout, ContextLayout (prefetch on mount), InlineChatButton, SchemaTree sidebar indicators, ContextSidebar indicators

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Get conversations referencing a context object | Request: `{contextType, contextId}` -> Response: `ConversationRef[]` | Sidebar badges, InlineChatButton popover |

**ConversationRef shape:** `{id, title}`

### Domain 7: Dashboard Stats

**Consumers:** OverviewDashboard

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Aggregate counts | `{schemaCount, tableCount, conceptCount, queryCount}` | Stat cards on home page |

### Domain 8: Feature Flags

**Consumers:** FeatureFlagContext (app-wide), every view and component via `useFeatureFlags()`

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Get flags for current session | `Partial<FeatureFlags>` -- only overrides; omitted keys default to `true` | All views, all feature-gated UI elements |

Full FeatureFlags interface has 70 boolean flags across 14 categories (views, chat references, inline chat, model view, knowledge view, analysis view, sidebar, connect, admin, profile, login providers, related content, chat input controls, header/chrome). See `src/features/defaults.ts` for complete list.

### Domain 9: Global Search

**Consumers:** GlobalSearch (header component), ChatArea (Ask in Chat auto-send)

| Requirement | Expected Shape | UI Consumer |
|---|---|---|
| Search across all object types | Request: `{query: string}` -> Response: `SearchResult[]` | GlobalSearch dropdown results |

**SearchResult shape:** `{id, name, type: 'view'|'schema'|'table'|'attribute'|'concept'|'query', description?, breadcrumb?, route}`

**Key behaviors:**
- Queries shorter than 2 characters return empty results
- Results are grouped by type (views, schema entities, concepts, saved queries)
- Max 20 results returned
- Case-insensitive matching against name, description, tags, synonyms
- "Ask in Chat" fallback when no results — navigates to general chat with the search query pre-filled
- Gated by `headerGlobalSearch` feature flag

---

## Part 2: Existing Backend Inventory

### mill-metadata-service (`/api/metadata/v1`)

| Endpoint | Method | Response Type |
|---|---|---|
| `/entities` | GET | `List<MetadataEntityDto>` |
| `/entities/{id}` | GET | `MetadataEntityDto` |
| `/schemas/{schema}/tables/{table}` | GET | `MetadataEntityDto` |
| `/schemas/{schema}/tables/{table}/attributes/{attribute}` | GET | `MetadataEntityDto` |
| `/entities/{id}/facets/{facetType}` | GET | `FacetDto` |
| `/entities/{id}/facets/{facetType}/scopes` | GET | `Set<String>` |
| `/entities/{id}/related` | GET | `List<MetadataEntityDto>` |
| `/facets/entities/{entityId}/types/{facetType}` | GET | `FacetDto` |
| `/explorer/tree` | GET | `List<TreeNodeDto>` |
| `/explorer/search` | GET | `List<SearchResultDto>` |
| `/explorer/lineage` | GET | `Map<String, Object>` |

### mill-ai-nlsql-chat-service (`/api/nl2sql`)

| Endpoint | Method | Response Type |
|---|---|---|
| `/chats` | GET | `List<Chat>` |
| `/chats` | POST | `Chat` |
| `/chats/{chatId}` | GET | `Chat` |
| `/chats/{chatId}` | PATCH | `Chat` |
| `/chats/{chatId}` | DELETE | 204 |
| `/chats/{chatId}/messages` | GET | `List<ChatMessage>` |
| `/chats/{chatId}/messages` | POST | `ChatMessage` |
| `/chats/{chatId}/stream` | GET | `Flux<ServerSentEvent>` |

### mill-data-http-service (`/services/jet`)

| Endpoint | Method | Request/Response Format |
|---|---|---|
| `/Handshake` | POST | Protobuf / JSON |
| `/ListSchemas` | POST | Protobuf / JSON |
| `/GetSchema` | POST | Protobuf / JSON |
| `/ParseSql` | POST | Protobuf / JSON |
| `/SubmitQuery` | POST | `QueryRequest` -> `QueryResultResponse` (Protobuf) |
| `/FetchQueryResult` | POST | `QueryResultRequest` -> `QueryResultResponse` (Protobuf) |

### mill-well-known-service (`/.well-known`)

| Endpoint | Method | Response Type |
|---|---|---|
| `/mill` | GET | `ApplicationDescriptor` |

---

## Part 3: Gap Analysis

### Legend

- **COVERED** -- Backend endpoint exists and can serve the UI requirement with minor or no adaptation
- **PARTIAL** -- Backend endpoint exists but has shape/path/protocol mismatches requiring adaptation
- **MISSING** -- No backend endpoint exists

### Gap Matrix

| # | UI Requirement | Backend Status | Existing Endpoint | Issues |
|---|---|---|---|---|
| G-1 | Schema tree | **PARTIAL** | `GET /api/metadata/v1/explorer/tree` | `TreeNodeDto` shape may differ from `SchemaEntity` (field names, nesting). Need to verify if `type` field uses `SCHEMA/TABLE/ATTRIBUTE` enum and if `children` recursion matches. `scope` query param not used by UI. |
| G-2 | Entity lookup | **PARTIAL** | `GET /api/metadata/v1/entities/{id}` | `MetadataEntityDto` vs `SchemaEntity` shape. Dot-separated IDs (`sales.customers.customer_id`) must be supported. `scope` query param not used by UI. |
| G-3 | Entity facets (composite) | **PARTIAL** | `GET /api/metadata/v1/entities/{id}/facets/{facetType}` | Backend requires separate call per facet type. UI expects **all facets in a single response**. Either need composite endpoint or UI makes 3 parallel calls. Also: `FacetDto` shape vs `EntityFacets` shape alignment unknown. |
| G-4 | Concepts list | **MISSING** | -- | Entire concepts/knowledge domain absent from backend. |
| G-5 | Concept by ID | **MISSING** | -- | Same as G-4. |
| G-6 | Concept categories | **MISSING** | -- | Same as G-4. |
| G-7 | Concept tags | **MISSING** | -- | Same as G-4. |
| G-8 | Saved queries list | **MISSING** | -- | No saved query persistence. |
| G-9 | Saved query by ID | **MISSING** | -- | Same as G-8. |
| G-10 | Execute SQL (JSON) | **PARTIAL** | `POST /services/jet/SubmitQuery` | Exists but uses protobuf `QueryRequest`/`QueryResultResponse`. UI expects simple JSON `{sql}` -> `{columns, rows, rowCount, executionTimeMs}`. Need REST JSON wrapper or adapter endpoint. |
| G-11 | Conversations list | **PARTIAL** | `GET /api/nl2sql/chats` | Path and DTO naming differ (`Chat` vs `Conversation`, `ChatMessage` vs `Message`). Need to verify: does `Chat` include nested messages? Does it have `createdAt`/`updatedAt`? |
| G-12 | Create conversation | **PARTIAL** | `POST /api/nl2sql/chats` | Request shape (`CreateChatRequest` vs `{title}`). Response shape alignment. |
| G-13 | Delete conversation | **PARTIAL** | `DELETE /api/nl2sql/chats/{id}` | Likely compatible. Verify 204 response. |
| G-14 | Chat message streaming | **PARTIAL** | `POST /chats/{id}/messages` + `GET /chats/{id}/stream` | Backend separates message submission from streaming. UI expects **single SSE endpoint** that accepts the message and streams the response. SSE event format (`ServerSentEvent` vs `data: {"chunk": "..."}`) needs verification. |
| G-15 | Inline chat (context-aware) | **MISSING** | -- | No stateless context-aware chat endpoint. Could potentially wrap nl2sql with context injection. |
| G-16 | Chat references | **MISSING** | -- | No endpoint to find conversations that reference a given entity/concept. |
| G-17 | Dashboard stats | **MISSING** | -- | No aggregate stats endpoint. Would need to query across metadata + concepts + queries. |
| G-18 | Feature flags | **MISSING** | -- | No feature flags endpoint. |
| G-19 | Global search | **PARTIAL** | `GET /api/metadata/v1/explorer/search` | Metadata service has a search endpoint but only covers schema entities. UI needs cross-domain search spanning views, schema, concepts, and saved queries. Concept/query search requires those domains to exist first (G-4, G-8). |

### Shape Discrepancy Details

**G-1/G-2 (Schema/Entity):** The metadata service uses `MetadataEntityDto` and `TreeNodeDto`. Key questions for backend:
- Does `TreeNodeDto` contain `{id, type, name, children}`?
- Is `type` an enum with values `SCHEMA`, `TABLE`, `ATTRIBUTE`?
- Are entity IDs dot-separated (`schema.table.attribute`)?

**G-3 (Facets):** The metadata service exposes `/entities/{id}/facets/{facetType}` requiring the facet type as a path parameter. The UI needs all three facet types (`descriptive`, `structural`, `relations`) at once. Options:
1. Add a composite `GET /entities/{id}/facets` endpoint returning all types
2. UI makes 3 parallel requests (less clean but no backend change)

**G-10 (Query Execution):** The jet service uses protobuf-shaped `QueryRequest` (wraps `SQLStatement`) and returns `QueryResultResponse` (wraps `VectorBlock` columnar format). The UI expects flat JSON `{columns: [{name, type}], rows: [{...}], rowCount, executionTimeMs}`. This is a significant format gap requiring either:
1. A new REST controller that wraps jet and returns simple JSON
2. Or: expose the existing metadata-level query functionality differently

**G-14 (Chat Streaming):** The nl2sql service has separate POST (send message) and GET (stream) endpoints. The UI calls a single async generator that sends and streams in one operation. The backend flow needs to be: POST message -> immediately GET stream, or: single POST endpoint that returns SSE.

---

## Part 4: Backend Development Backlog

### Tier 1 -- Adapt Existing Endpoints (low effort, high value)

These require thin wrappers, composite endpoints, or shape alignment over already-implemented backend logic.

| ID | Task | Description | Depends On |
|---|---|---|---|
| **B-1** | Verify and document schema tree DTO shape | Confirm `TreeNodeDto` fields match `{id, type, name, children}`. Document any mapping needed. | -- |
| **B-2** | Verify and document entity DTO shape | Confirm `MetadataEntityDto` fields and dot-separated ID support. | -- |
| **B-3** | Composite facets endpoint | Add `GET /api/metadata/v1/entities/{id}/facets` (no facetType) returning `{descriptive, structural, relations}` in one response. | -- |
| **B-4** | JSON query execution wrapper | Add REST endpoint accepting `{sql: string}` and returning `{columns, rows, rowCount, executionTimeMs}` by wrapping jet's SubmitQuery and converting VectorBlock to flat JSON. | -- |
| **B-5** | Verify chat DTO compatibility | Document `Chat`/`ChatMessage` shape from nl2sql. Confirm: nested messages in list response, timestamp format, title field. | -- |
| **B-6** | Unified chat send+stream endpoint | Either: (a) make `POST /chats/{id}/messages` return SSE directly, or (b) document the two-step flow so the UI can adapt. | B-5 |

### Tier 2 -- New Domain: Concepts/Knowledge (medium effort, high value)

Entire new domain -- the Knowledge view has no backend support at all.

| ID | Task | Description | Depends On |
|---|---|---|---|
| **B-7** | Concepts data model | Design JPA entity for `Concept` with fields: id, name, category, tags (collection), description, sql, relatedEntities (collection), source (enum), createdAt, updatedAt. | -- |
| **B-8** | `GET /api/v1/concepts` | List concepts with optional `?category=X` and `?tag=X` query filters. Return `Concept[]`. | B-7 |
| **B-9** | `GET /api/v1/concepts/{id}` | Single concept lookup. Return `Concept` or 404. | B-7 |
| **B-10** | `GET /api/v1/concepts/categories` | Aggregate query returning `{name, count}[]` grouped by category. | B-7 |
| **B-11** | `GET /api/v1/concepts/tags` | Aggregate query returning `{name, count}[]` from tags collection. | B-7 |

### Tier 3 -- New Domain: Saved Queries (medium effort, medium value)

The Analysis view lists saved queries but has no persistence backend.

| ID | Task | Description | Depends On |
|---|---|---|---|
| **B-12** | Saved queries data model | JPA entity for `SavedQuery`: id, name, description, sql, tags (collection), createdAt, updatedAt. Consider per-user ownership. | -- |
| **B-13** | `GET /api/v1/queries` | List saved queries. | B-12 |
| **B-14** | `GET /api/v1/queries/{id}` | Single saved query lookup. | B-12 |
| **B-15** | `POST /api/v1/queries` | Create saved query. | B-12 |
| **B-16** | `PUT /api/v1/queries/{id}` | Update saved query. | B-12 |
| **B-17** | `DELETE /api/v1/queries/{id}` | Delete saved query. | B-12 |

### Tier 4 -- New Endpoints: Supporting Features (low-medium effort)

| ID | Task | Description | Depends On |
|---|---|---|---|
| **B-18** | `GET /api/v1/stats` | Aggregate counts: schemas (from metadata), tables (from metadata), concepts (from B-7), queries (from B-12). | B-7, B-12 |
| **B-19** | `GET /api/v1/features` | Return `Partial<FeatureFlags>` for current user/session. Can start as static config file, evolve to per-user/per-tenant. | -- |
| **B-20** | Inline chat endpoint | `POST /api/v1/inline-chat/messages` accepting `{contextType, contextId, message}`, returning SSE stream. Wraps AI service with context injection (load entity metadata for model context, concept definition for knowledge, query for analysis). | B-3, B-9 |
| **B-21** | Chat references endpoint | `GET /api/v1/chat-references?contextType=X&contextId=Y` returning `ConversationRef[]`. Searches chat message history for mentions of the given entity/concept. | B-5 |
| **B-22** | Global search endpoint | `GET /api/v1/search?q=...` returning `SearchResult[]`. Cross-domain search spanning schema entities (from metadata), concepts (from B-7), saved queries (from B-12), and static views. Max 20 results, grouped by type, case-insensitive. | B-7, B-12 |

### Tier 5 -- Future Enhancements

| ID | Task | Description |
|---|---|---|
| **B-23** | Authentication integration | OAuth2/OIDC provider, Bearer token validation, user identity in API context |
| **B-24** | Per-user scoping | Saved queries, conversations, feature flags scoped to authenticated user |
| **B-25** | Concept write operations | POST/PUT/DELETE for concepts (currently UI is read-only) |
| **B-26** | Query execution history | Track and expose per-user execution history |
| **B-27** | Data lineage exposure | Expose `/explorer/lineage` for future UI lineage view |

---

## Part 5: Open Questions for Backend Team

These must be answered before frontend integration can proceed:

1. **TreeNodeDto shape** -- Does it match `{id: string, type: 'SCHEMA'|'TABLE'|'ATTRIBUTE', name: string, children: TreeNodeDto[]}`? Or does it use different field names / enum values?
2. **Entity ID format** -- Are entity IDs dot-separated (`sales.customers.customer_id`) or use a different convention?
3. **FacetDto shape** -- Does `FacetDto` from the metadata service align with the `DescriptiveFacet`/`StructuralFacet`/`RelationFacet` types the UI expects? Is facet type a path param enum?
4. **Chat/ChatMessage shape** -- Does the nl2sql `Chat` object include nested messages? What are the field names? Does `ChatMessage` have `role: 'user'|'assistant'`?
5. **SSE event format** -- What is the exact SSE event structure from `/chats/{id}/stream`? Is it `data: {"chunk": "..."}` or a different format?
6. **VectorBlock to JSON** -- Is there an existing utility to convert `QueryResultResponse` / `VectorBlock` to flat `{columns, rows}` JSON, or does this need to be built?
7. **Concepts domain ownership** -- Should concepts live in the metadata service (extending it) or in a new dedicated service?
8. **Multi-tenancy / user scoping** -- Are saved queries and conversations scoped per-user from the start, or global initially?
9. **Search result shape** -- The UI expects `{id, name, type, description?, breadcrumb?, route}`. Does the existing `/explorer/search` return something compatible? What does `SearchResultDto` look like?

---

## Summary: Effort Estimation

| Tier | Items | Description | Relative Effort |
|---|---|---|---|
| **Tier 1** | B-1 through B-6 | Adapt existing endpoints | Small -- verification + thin wrappers |
| **Tier 2** | B-7 through B-11 | Concepts/Knowledge domain | Medium -- new JPA entity + controller + 4 endpoints |
| **Tier 3** | B-12 through B-17 | Saved Queries domain | Medium -- new JPA entity + controller + 5 endpoints |
| **Tier 4** | B-18 through B-22 | Supporting features | Medium -- cross-service aggregation + AI wrapper + search |
| **Tier 5** | B-23 through B-27 | Future enhancements | Large -- auth, scoping, new UI capabilities |

---

*Generated: February 12, 2026*
