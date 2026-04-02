# UI Element Inventory

Complete inventory of UI elements across `ui/mill-ui`, organized by runtime UI hierarchy.

For each element:
- **Description**: what the element does
- **Content source**: where content/data comes from (including payload composition)
- **Feature flag**: flag name when applicable
- **Default**: current default from `defaultFeatureFlags`
- **Backend state**: `real`, `mock`, `mixed`, or `n/a`

---

## Backend State Legend

- **real**: element content comes from real backend endpoints currently in use
- **mock**: element content comes from mock services or static mock data
- **mixed**: element combines real + mock sources
- **n/a**: purely presentational or local UI state, no backend dependency

---

## Service Source of Truth (Used by UI)

| Service | Current implementation | Backend state |
|---|---|---|
| `authService` | real REST calls (`/auth/*`, `/.well-known/mill`) | real |
| `schemaService` | real REST calls (`/api/v1/schema/*`), returns schema/table/column DTOs and facet envelopes (`descriptive`, `structural`, `relation`) | real |
| `conceptService` | `mockConcepts` (concept identity, category, tags, SQL, related entities) | mock |
| `queryService` | `mockQueries` (saved queries + simulated execution result tables) | mock |
| `statsService` | mock aggregate from mock schema/concept/query data | mock |
| `chatService` | local mock response pools by context type (`general`, `model`, `knowledge`, `analysis`) | mock |
| `chatReferencesService` | deterministic mock refs (`ConversationRef[]`: chat id/title list) | mock |
| `relatedContentService` | deterministic mock cross-object refs (`RelatedContentRef[]`: model/concept/analysis links) | mock |
| `featureService` | mock returns full `defaultFeatureFlags`; real client should return `Partial<FeatureFlags>` from `GET /api/v1/features` for merge in `FeatureFlagProvider` | mock |
| `searchService` | mock search over mock data | mock |

---

## 1) App Root Hierarchy

### 1.1 App boot and shell

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| App loading screen (`Loader`) | Full-page loader while auth/app-name resolve | local loading state | n/a | n/a | n/a |
| Notifications container | Global toast host | local + feature behavior | n/a | n/a | n/a |
| Inline chat drawer mount | Right drawer host for inline chat sessions | inline chat context state containing per-context message threads and active session id | `inlineChatEnabled` | `false` | mock |
| Route switch | Main route composition and auth gating | router + auth state | view flags (per route) | see below | mixed |

### 1.2 Top-level route visibility flags

| Route | Flag | Default |
|---|---|---|
| `/home` | `viewHome` | `true` |
| `/model` | `viewModel` | `true` |
| `/knowledge` | `viewKnowledge` | `false` |
| `/analysis` | `viewAnalysis` | `false` |
| `/chat` | `viewChat` | `false` |
| `/connect` | `viewConnect` | `false` |
| `/admin` | `viewAdmin` | `true` |
| `/profile` | `viewProfile` | `true` |

---

## 2) Global Header (`AppHeader`)

### 2.1 Brand and global nav row

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Brand logo + app name | Clickable identity; navigates to Home | app name from `/.well-known/mill` fallback to `Mill` | n/a | n/a | real |
| Main nav buttons (Model/Knowledge/Analysis/Chat/Connect) | Primary navigation tabs | static config + routing | `viewModel`, `viewKnowledge`, `viewAnalysis`, `viewChat`, `viewConnect` | Model `true`; Knowledge/Analysis/Chat/Connect `false` | n/a |
| Right nav button (Admin) | Admin navigation | static config + routing | `viewAdmin` | `true` | n/a |
| Global search trigger | Opens global search | `searchService` over mixed index: views + mock schema/facets + mock concepts + mock queries | `headerGlobalSearch` | `false` | mock |

### 2.2 User menu (right side)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| User icon trigger | Opens account menu | auth context | `headerUserProfile` | `true` | real |
| Avatar + name + email | Current user identity panel | `authService.getMe()` | `headerUserProfile` | `true` | real |
| Theme switch row (dark/light + swatches) | Theme mode and palette chooser | local theme state | `headerThemeSwitcher` | `true` | n/a |
| Profile menu item | Link to `/profile` | router | `viewProfile` | `true` | n/a |
| Logout menu item | Ends backend session | `authService.logout()` | user security state | n/a | real |

---

## 3) Auth Pages

## 3.1 Login page (`/login`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Social login buttons | Provider-specific auth actions | local UI wiring | `loginGithub`, `loginGoogle`, `loginMicrosoft`, `loginAws`, `loginAzure` | all `false` | n/a |
| Email/password form | Local auth login form | `authService.login()` | `loginPassword` | `true` | real |
| Registration link | Navigate to register page | router | `loginRegistration` | `true` | n/a |

## 3.2 Register page (`/register`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Registration form | Creates account and session | `authService.register()` | n/a | n/a | real |

---

## 4) Home View (`/home`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Stats cards (schemas/tables/concepts/queries) | Dashboard counters | `statsService` from `mockSchemaTree`, `mockConcepts`, `mockSavedQueries` | `viewHome` | `true` | mock |
| Quick links | Navigate to major views | route config | view flags | per §1.2 | n/a |

---

## 5) Model View (`/model/:schema?/:table?/:attribute?`)

Hierarchy:
1. `DataModelLayout`
2. left `SchemaTree`
3. right `EntityDetails`
4. facet tabs (`Descriptive`, `Structural`, `Relations`)

### 5.1 DataModelLayout shell

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Schema browser sidebar container | Left panel host | local layout | `sidebarCollapsible` | `true` | n/a |
| Tree loading state | Spinner while context/tree resolves | local async state | n/a | n/a | n/a |
| Details loading state | Spinner while entity/facets load | local async state | n/a | n/a | n/a |
| Empty-state welcome panel | Default right pane when nothing selected | static text | n/a | n/a | n/a |

### 5.2 SchemaTree (left tree)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Schema/Table/Column nodes | Hierarchical object navigation | `schemaService.getTree(context)` for schema+table graph, then `getEntityById` for lazy table column hydration | `viewModel` | `true` | real |
| Expand/collapse chevrons | Tree open/close controls | local component state | n/a | n/a | n/a |
| Active session chat icon (small bubble) | Marks nodes with active inline session | inline chat context (`session.contextId === node.id`) | `inlineChatEnabled` (+ per-context flags) | `false` | mock |
| Violet chat-reference count pill in tree | Count of related general-chat refs | `chatReferencesService` via context cache; payload is related chat list (`id`, `title`) | `chatReferencesEnabled`, `chatReferencesSidebarIndicator`, `chatReferencesModelContext` | `false`, `false`, `false` | mock |

### 5.3 EntityDetails header (right panel)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Entity icon + title + type badge | Selected entity identity | `schemaService.getEntityById()` + descriptive facet fallback | n/a | n/a | real |
| Entity ID line | Fully qualified id display | selected entity object | n/a | n/a | real |
| RelatedContent button | Cross-object related content popover | `relatedContentService`; can include model tree nodes plus concept/query refs in one payload | `relatedContentEnabled` (+ model-level flags) | `false` | mock |
| InlineChat button | Start/open inline contextual chat | inline chat context + `chatService` context-aware response pool; can coexist with related chats and related content | `inlineChatEnabled` (+ model-level flags) | `false` | mock |

### 5.4 EntityDetails quick badges + base panel

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| PK/FK/Unique/Not Null badges | Structural quick markers | structural facet payload | `modelQuickBadges` | `false` | real |
| Type badge in quick row | Data type marker (`type`/`physicalType`) | structural facet payload | `modelPhysicalType` | `true` | real |
| Base details card (ID/type/table count/column position) | Always-visible structural summary | selected entity fields | n/a | n/a | real |

### 5.5 Facet tabs (EntityDetails content)

Facet tabs are grouped by **manifest category** (from `facetTypeService` / facet type list). Each facet type renders with the **descriptor-driven** read/edit path (`EntityDetails`), except the structural facet read branch which can use the tailored `StructuralFacet` component when `modelStructuralFacet` is on and structural data exists.

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Category tabs + facet cards | One or more cards per facet type; `MULTIPLE` cardinality uses one card per stored instance | `GET .../facets` array + facet type manifests; payloads edited via PUT/DELETE facet APIs | `modelStructuralFacet` (structural read view only); other facets ungated by flags | `true` | real |
| No-facets empty message | Displayed when no tab has content | local conditional rendering | n/a | n/a | n/a |

---

## 6) Knowledge View (`/knowledge/:conceptId?`)

### 6.1 Context sidebar

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Concept list | Browsable concepts | `conceptService.getConcepts()` from `mockConcepts` (name/category/source/tags) | `viewKnowledge` | `false` | mock |
| Category filter section | Filter concepts by category | `conceptService.getCategories()` | `sidebarKnowledgeCategories` | `true` | mock |
| Tag filter section | Filter concepts by tags | `conceptService.getTags()` | `sidebarKnowledgeTags` | `true` | mock |
| Chat-reference indicators in list | Related-chat count markers | `chatReferencesService` conversation-ref payload (`id`, `title`) | `chatReferencesEnabled`, `chatReferencesSidebarIndicator`, `chatReferencesKnowledgeContext` | `false`, `false`, `false` | mock |

### 6.2 Concept details panel

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Concept title/header | Core concept identity | `conceptService.getConceptById()` | n/a | n/a | mock |
| Description block | Human-readable explanation | concept payload | `knowledgeDescription` | `true` | mock |
| Tags | Concept tags badge list | concept payload | `knowledgeTags` | `true` | mock |
| SQL definition | SQL definition section | concept payload | `knowledgeSqlDefinition` | `true` | mock |
| Related entities section | Linked model entities | concept payload | `knowledgeRelatedEntities` | `true` | mock |
| Metadata section | Created/updated details | concept payload | `knowledgeMetadata` | `true` | mock |
| Source badge | Origin marker (manual/inferred/etc.) | concept payload | `knowledgeSourceBadge` | `true` | mock |
| RelatedContent button | Cross-object related content | `relatedContentService` payload can include related model entities, concepts, and analysis queries | relatedContent knowledge flags | `false` | mock |
| InlineChat button | Contextual inline chat | inline chat context + `chatService` knowledge response pool and session state | inlineChat knowledge flags | `false` | mock |

---

## 7) Analysis View (`/analysis/:queryId?`)

### 7.1 Query sidebar

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Saved query list | Query selection list | `queryService.getSavedQueries()` from `mockSavedQueries` (id/name/sql/description/tags) | `viewAnalysis` | `false` | mock |
| Query count badge | Shows list count | query list length | `sidebarAnalysisBadge` | `true` | mock |
| New query action | Creates in-memory query | local state | n/a | n/a | n/a |
| Delete query action | Removes query from in-memory list | local state | n/a | n/a | n/a |

### 7.2 Query editor + toolbar

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| SQL editor textarea | Query text editing | local state | n/a | n/a | n/a |
| Format SQL action | Formats SQL text | client formatter | `analysisFormatSql` | `true` | n/a |
| Copy SQL action | Copy current SQL | browser clipboard | `analysisCopySql` | `true` | n/a |
| Clear SQL action | Clears editor | local state | `analysisClearSql` | `true` | n/a |
| Execute action | Runs query | `queryService.executeQuery()` returns simulated columns/rows/executionTime metadata | `analysisExecuteQuery` | `true` | mock |
| InlineChat button | Query-context inline assistant | inline chat context + `chatService` analysis response pool (query tuning/explanation style outputs) | inlineChat analysis flags | `false` | mock |

### 7.3 Query results panel

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Results table | Data rows + sortable headers | `queryService.executeQuery()` response with typed column schema + row matrix | `analysisQueryResults` | `true` | mock |
| Status/row count | Execution metadata | query response metadata | `analysisQueryResults` | `true` | mock |
| Export controls | CSV/TSV/JSON export actions | current result set | `analysisQueryResults` | `true` | mock |

---

## 8) Chat View (`/chat/*`)

### 8.1 AppShell + sidebar

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Conversation sidebar list | Existing general chats | `chatService.listChats()` mock chat summaries (`chatId`, `chatName`, `updatedAt`) | `viewChat` | `false` | mock |
| New chat button | Creates chat session | `chatService.createChat()` creates general or context chat records | `viewChat` | `false` | mock |
| Delete chat controls | Removes chat from context state | local/chat context state | n/a | n/a | mock |

### 8.2 Chat area

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Message list | Conversation transcript | chat context + `chatService.sendMessage()` streaming chunks from mock response pools | `viewChat` | `false` | mock |
| Typing indicator | Assistant response in-progress marker | local streaming state | n/a | n/a | mock |
| Input box | Prompt input and send | local state | `chatAttachButton`, `chatDictateButton` | `true`, `true` | n/a |

---

## 9) Inline Chat Drawer (Global, Right Side)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Drawer shell | Inline session container | inline chat context state | `inlineChatEnabled` | `false` | mock |
| Session switcher/popover | Multi-session management | inline chat context state | `inlineChatMultiSession`, `inlineChatSessionGrouping` | `false`, `false` | mock |
| Inline message panel | Per-session transcript | `chatService.sendMessage()` with context-aware pool by `contextType` | `inlineChatEnabled` | `false` | mock |
| Related content in drawer | Related object popover | `relatedContentService` (model/concept/analysis refs; model refs may include schema-table-column chain) | `relatedContentInDrawer` | `false` | mock |
| Related chats in drawer | Related general-chat refs | `chatReferencesService` (`ConversationRef[]` shown as related conversations list) | `chatReferencesEnabled` | `false` | mock |

---

## 10) Connect View (`/connect/:section?`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Connect root page | Integration/how-to shell | static docs-like content | `viewConnect` | `false` | n/a |
| Services section | Service connection guide | static content | `connectServices` | `true` | n/a |
| Python section | Python integration guide | static content | `connectPython` | `true` | n/a |
| Java section | Java integration guide | static content | `connectJava` | `true` | n/a |

---

## 11) Admin View (`/admin/:section?`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Admin page shell | Admin navigation and section content | mostly static placeholders | `viewAdmin` | `true` | n/a |
| Data Sources section | Admin subsection | static placeholder | `adminDataSources` | `true` | n/a |
| Policies section | Admin subsection | static placeholder | `adminPolicies` | `true` | n/a |
| Services section | Admin subsection | static placeholder | `adminServices` | `true` | n/a |
| Settings section | Admin subsection | static placeholder | `adminSettings` | `true` | n/a |
| Model sidebar group (planned) | Admin left-nav group for model governance tools | admin navigation config | `adminModelNavEnabled` | `true` | n/a |
| Facet Types nav item (planned) | Subitem under Model group linking to facet type management | admin navigation config + router | `adminFacetTypesEnabled` | `true` | n/a |
| Facet Types page shell (planned) | Management page for descriptor list/create/edit/delete | facet type REST endpoints (`/api/v1/metadata/facets*`) | `adminFacetTypesEnabled` | `true` | real |
| Facet Types create/edit actions (planned) | Authoring controls for descriptor metadata and fields | local form state + facet type REST write endpoints | `facetTypesReadOnly` | `false` | real |
| Facet Types read-only banner (planned) | Explains that facet types are read-only in current registry mode (e.g. portal) | feature flag state today; later server descriptor flags (`.well-known`) | `facetTypesReadOnly` | `false` | mixed |

---

## 12) Profile View (`/profile/:section?`)

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Profile page shell | User profile workspace | auth context + static sections | `viewProfile` | `true` | mixed |
| General section | User profile information | auth/profile data | `profileGeneral` | `true` | real |
| Settings section | User preferences area | mostly local/static | `profileSettings` | `true` | mixed |
| Access section | Access/API/security area | auth/profile context + static | `profileAccess` | `true` | mixed |

---

## 13) Error and Utility Pages

| Element | Description | Content source | Feature flag | Default | Backend state |
|---|---|---|---|---|---|
| Not Found page (`*`) | Route miss fallback | static content | n/a | n/a | n/a |
| Access Denied page | Permission failure page | static content | n/a | n/a | n/a |

---

## 14) Indicator/Pill/Badge Inventory (Cross-cutting)

This section explicitly inventories pills/badges/indicators currently used in UI.

| Indicator | Location | Meaning | Source | Flag | Default | Backend state |
|---|---|---|---|---|---|---|
| Violet count pill (tree) | `SchemaTree` | related general chats count | `chatReferencesService` payload (`ConversationRef[]`: conversation titles/ids for that node) | `chatReferencesEnabled` + `chatReferencesSidebarIndicator` + `chatReferencesModelContext` | `false` + `false` + `false` | mock |
| Chat bubble icon (tree) | `SchemaTree` | active inline chat session exists | inline chat context | `inlineChatEnabled` | `false` | mock |
| PK/FK/Unique/Not Null badges | `EntityDetails` | structural constraints | structural facet payload | `modelQuickBadges` | `false` | real |
| Type badge (details quick row) | `EntityDetails` | data type marker | structural facet payload | `modelPhysicalType` | `true` | real |
| Cardinality badge | Relation facet cards (`EntityDetails`) | relation cardinality (`1:1`, `1:N`, etc.) | relation facet payload (descriptor-driven MULTIPLE cards) | n/a | n/a | real |
| Relation type badge | Relation facet cards (`EntityDetails`) | relation type (`FOREIGN_KEY`, etc.) | relation facet payload | n/a | n/a | real |
| Related content count badge | `RelatedContentButton` | cross-object related content count | `relatedContentService` payload may include concept refs, analysis refs, and model hierarchy refs together | `relatedContentEnabled` | `false` | mock |
| Inline chat active dot/badge | `InlineChatButton` | inline session and/or related chat presence | inline chat session state + `chatReferencesService` related-conversation refs | `inlineChatEnabled`, `chatReferencesEnabled` | `false`, `false` | mock |

---

## 15) Notes and Known Gaps

1. The inventory is complete for **currently mounted UI elements** in the active app hierarchy.
2. Model relation indicators now derive from real relation facets in `schemaService`.
3. Chat-reference and related-content indicators are still mock-backed (conversation refs and cross-object refs are deterministic mock payloads).
4. If/when services are swapped to real backends, backend-state column should be updated first in Section 0 and Section 14.
5. Admin facet type management (list/create/edit) is live behind `adminModelNavEnabled`, `adminFacetTypesEnabled`, and `facetTypesReadOnly`.

---

*Last updated: 2026-04-02*

