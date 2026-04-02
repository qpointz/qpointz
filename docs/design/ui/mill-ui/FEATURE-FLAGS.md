# Feature Flags Inventory

Complete inventory of all feature flags in the application. All flags are boolean and are defined in `src/features/defaults.ts`. **Defaults are mixed:** many areas default to **`false`** (opt-in) so a minimal shell can run without Chat, Knowledge, Analysis, Connect, global search, chat references, inline chat, or related content unless the backend overrides or you edit defaults.

## Architecture

- **Definition**: `src/features/defaults.ts` — `FeatureFlags` interface + `defaultFeatureFlags` object
- **Provider**: `src/features/FeatureFlagContext.tsx` — `FeatureFlagProvider` calls `featureService.getFlags()` on mount and merges the result over `defaultFeatureFlags` (production wiring targets `GET /api/v1/features`)
- **Hook**: `useFeatureFlags()` — returns the resolved `FeatureFlags` object
- **Backend**: `GET /api/v1/features` — returns `Partial<FeatureFlags>`. **Omitted keys keep `defaultFeatureFlags` values** (not forced to `true`). If the request **fails**, the app uses **`defaultFeatureFlags`** unchanged.
- **Total flags**: **74** across **14** categories

## Flag Inventory

### 1. Views (5 flags)

Top-level navigation routes. Disabling a flag hides the route and its nav button.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewHome` | `true` | Home / Overview dashboard route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewModel` | `true` | Data Model explorer route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewKnowledge` | `false` | Knowledge / Concepts route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewAnalysis` | `false` | Analysis / Query Playground route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewChat` | `false` | General Chat route and nav button | `App.tsx`, `AppHeader.tsx` |

### 2. Chat References (5 flags)

Control the **related conversations** feature (links to existing General Chat threads — **not** inline chat). Sidebar count pills require the master flag, **`chatReferencesSidebarIndicator`**, and the matching context flag (`Model` vs `Knowledge`).

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `chatReferencesEnabled` | `false` | Master toggle for all chat reference features | `ChatReferencesContext.tsx`, `InlineChatDrawer.tsx`, `SchemaTree.tsx`, `ContextSidebar.tsx` |
| `chatReferencesModelContext` | `false` | Load/show chat references for Data Model entities | `ChatReferencesContext.tsx` (fetch gating), `SchemaTree.tsx` (sidebar pill) |
| `chatReferencesKnowledgeContext` | `false` | Chat references on Knowledge concepts | `ChatReferencesContext.tsx`, `ContextSidebar.tsx` (sidebar pill) |
| `chatReferencesAnalysisContext` | `false` | Chat references on Analysis queries | `ChatReferencesContext.tsx` |
| `chatReferencesSidebarIndicator` | `false` | Violet count badges beside Model / Knowledge sidebar items | `SchemaTree.tsx`, `ContextSidebar.tsx` |

### 3. Inline Chat (10 flags)

Control the context-aware inline chat drawer that appears on detail pages.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `inlineChatEnabled` | `false` | Master toggle — hides the entire InlineChatDrawer when `false` | `App.tsx`, `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelContext` | `false` | Allow inline chat on Data Model entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelSchema` | `false` | Allow inline chat on SCHEMA-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelTable` | `false` | Allow inline chat on TABLE-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelColumn` | `false` | Allow inline chat on ATTRIBUTE-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatKnowledgeContext` | `false` | Allow inline chat on Knowledge concepts | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatAnalysisContext` | `false` | Allow inline chat on Analysis queries | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatMultiSession` | `false` | Allow multiple simultaneous inline chat sessions. When `false`, opening a new context replaces the existing session. | `InlineChatContext.tsx`, `InlineChatDrawer.tsx` |
| `inlineChatSessionGrouping` | `false` | Group inline sessions by route context type in the drawer tab bar | `InlineChatDrawer.tsx` |
| `inlineChatGreeting` | `false` | Show welcome/greeting message when a new inline chat session starts | `InlineChatContext.tsx` |

### 4. Model View Details (3 flags)

Control structural read UI and header badges on the Entity Details panel. Descriptive, relation, and other non-structural facets use the **descriptor-driven** renderer in `EntityDetails.tsx` (no separate flags).

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `modelStructuralFacet` | `true` | When structural data exists, use the tailored `StructuralFacet` read view for the structural facet; edit still uses the manifest/schema path | `EntityDetails.tsx` |
| `modelQuickBadges` | `false` | Quick info badges below entity header (PK, FK, nullable, unique) | `EntityDetails.tsx` |
| `modelPhysicalType` | `true` | Physical type badge on attribute detail | `EntityDetails.tsx` |

### 5. Knowledge View Details (6 flags)

Control which sections appear on the Concept Details panel.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `knowledgeDescription` | `true` | Description section | `ConceptDetails.tsx` |
| `knowledgeTags` | `true` | Tags badge list | `ConceptDetails.tsx` |
| `knowledgeSqlDefinition` | `true` | SQL definition code block | `ConceptDetails.tsx` |
| `knowledgeRelatedEntities` | `true` | Related schema entities list with navigation links | `ConceptDetails.tsx` |
| `knowledgeMetadata` | `true` | Created/updated timestamps | `ConceptDetails.tsx` |
| `knowledgeSourceBadge` | `true` | Source badge (MANUAL / INFERRED / IMPORTED) | `ConceptDetails.tsx` |

### 6. Analysis View Features (5 flags)

Control SQL editor toolbar buttons and result display.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `analysisFormatSql` | `true` | "Format SQL" toolbar button | `QueryEditor.tsx` |
| `analysisCopySql` | `true` | "Copy SQL" toolbar button | `QueryEditor.tsx` |
| `analysisClearSql` | `true` | "Clear" toolbar button | `QueryEditor.tsx` |
| `analysisExecuteQuery` | `true` | "Execute" button and Ctrl+Enter shortcut | `QueryEditor.tsx` |
| `analysisQueryResults` | `true` | Query results table below the editor | `QueryPlayground.tsx` |

### 7. Sidebar Features (4 flags)

Control sidebar behavior and filter sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `sidebarCollapsible` | `true` | Allow sidebar collapse/expand toggle | `CollapsibleSidebar.tsx` |
| `sidebarKnowledgeCategories` | `true` | Category filter section in Knowledge sidebar | `ContextSidebar.tsx` |
| `sidebarKnowledgeTags` | `true` | Tag filter section in Knowledge sidebar | `ContextSidebar.tsx` |
| `sidebarAnalysisBadge` | `true` | Query count badge in Analysis sidebar | `QueryPlayground.tsx` |

### 8. Connect View (4 flags)

Control the Connect view and its sub-sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewConnect` | `false` | Connect route and nav button | `App.tsx`, `AppHeader.tsx` |
| `connectServices` | `true` | Services connection guide section | `ConnectLayout.tsx` |
| `connectPython` | `true` | Python connection guide section | `ConnectLayout.tsx` |
| `connectJava` | `true` | Java connection guide section | `ConnectLayout.tsx` |

### 9. Admin View (8 flags)

Control the Admin panel and its sub-sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewAdmin` | `true` | Admin route and nav button | `App.tsx`, `AppHeader.tsx`, `OverviewDashboard.tsx` |
| `adminDataSources` | `true` | Data Sources management section | `AdminLayout.tsx` |
| `adminPolicies` | `true` | Policies management section | `AdminLayout.tsx` |
| `adminServices` | `true` | Services management section | `AdminLayout.tsx` |
| `adminSettings` | `true` | Settings management section | `AdminLayout.tsx` |
| `adminModelNavEnabled` | `true` | Show `Model` group in Admin left sidebar | `AdminLayout.tsx` |
| `adminFacetTypesEnabled` | `true` | Show `Facet Types` subitem and route in Admin Model group | `AdminLayout.tsx`, `FacetTypesListPage.tsx`, `FacetTypeEditPage.tsx` |
| `facetTypesReadOnly` | `false` | Force read-only facet type UI (disable create/edit/delete actions with tooltips) | `FacetTypesListPage.tsx`, `FacetTypeEditPage.tsx` |

### 10. Profile View (4 flags)

Control the Profile view and its sub-sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewProfile` | `true` | Profile route and menu item in user dropdown | `App.tsx`, `AppHeader.tsx` |
| `profileGeneral` | `true` | General profile section | `ProfileLayout.tsx` |
| `profileSettings` | `true` | Settings section | `ProfileLayout.tsx` |
| `profileAccess` | `true` | Access / API keys section | `ProfileLayout.tsx` |

### 11. Login Providers (7 flags)

Control which authentication methods and OAuth/SSO buttons appear on the login page.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `loginGithub` | `false` | "Continue with GitHub" button | `LoginPage.tsx` |
| `loginGoogle` | `false` | "Continue with Google" button | `LoginPage.tsx` |
| `loginMicrosoft` | `false` | "Continue with Microsoft" button | `LoginPage.tsx` |
| `loginAws` | `false` | "Continue with AWS" button | `LoginPage.tsx` |
| `loginAzure` | `false` | "Continue with Azure AD" button | `LoginPage.tsx` |
| `loginPassword` | `true` | Email/password form on login page | `LoginPage.tsx` |
| `loginRegistration` | `true` | Self-service registration link on login page | `LoginPage.tsx` |

### 12. Related Content (8 flags)

Control the "related content" feature — pills/popovers linking to related schema entities, concepts, and queries.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `relatedContentEnabled` | `false` | Master toggle for all related content features | `RelatedContentContext.tsx`, `RelatedContentButton.tsx`, `InlineChatDrawer.tsx` |
| `relatedContentModelContext` | `false` | Related content on Data Model entities | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentModelSchema` | `false` | Related content on SCHEMA-level entities | `RelatedContentButton.tsx` |
| `relatedContentModelTable` | `false` | Related content on TABLE-level entities | `RelatedContentButton.tsx` |
| `relatedContentModelColumn` | `false` | Related content on ATTRIBUTE-level entities | `RelatedContentButton.tsx` |
| `relatedContentKnowledgeContext` | `false` | Related content on Knowledge concepts | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentAnalysisContext` | `false` | Related content on Analysis queries | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentInDrawer` | `false` | Show related content section inside the inline chat drawer | `InlineChatDrawer.tsx` |

### 13. Chat Input Controls (2 flags)

Control optional buttons in the ChatInputBox (shared between general and inline chat).

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `chatAttachButton` | `true` | "+" attach button in ChatInputBox | `ChatInputBox.tsx` |
| `chatDictateButton` | `true` | Microphone/dictate button in ChatInputBox | `ChatInputBox.tsx` |

### 14. Header / Chrome (3 flags)

Control elements in the application header bar.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `headerGlobalSearch` | `false` | Search icon + floating search overlay in header nav | `AppHeader.tsx` |
| `headerThemeSwitcher` | `true` | Color theme and dark/light mode switcher in user menu | `AppHeader.tsx` |
| `headerUserProfile` | `true` | User avatar and dropdown menu in header | `AppHeader.tsx` |

---

## Flag Hierarchy

Some flags follow a master/child pattern. When the master is `false`, children are irrelevant (the entire feature is hidden).

```
chatReferencesEnabled
  ├── chatReferencesModelContext
  ├── chatReferencesKnowledgeContext
  ├── chatReferencesAnalysisContext
  └── chatReferencesSidebarIndicator

inlineChatEnabled
  ├── inlineChatModelContext
  │     ├── inlineChatModelSchema
  │     ├── inlineChatModelTable
  │     └── inlineChatModelColumn
  ├── inlineChatKnowledgeContext
  ├── inlineChatAnalysisContext
  ├── inlineChatMultiSession
  ├── inlineChatSessionGrouping
  └── inlineChatGreeting

relatedContentEnabled
  ├── relatedContentModelContext
  │     ├── relatedContentModelSchema
  │     ├── relatedContentModelTable
  │     └── relatedContentModelColumn
  ├── relatedContentKnowledgeContext
  ├── relatedContentAnalysisContext
  └── relatedContentInDrawer
```

---

## Backend Integration

### Endpoint

```
GET /api/v1/features
```

### Response

The backend returns `Partial<FeatureFlags>` — only flags it wants to override. Omitted flags fall back to `defaultFeatureFlags` values.

### Example: turn on General Chat and related UI for a demo

Built-in defaults already hide several routes and chat-related features. To enable them for one deployment, the API can return overrides such as:

```json
{
  "viewChat": true,
  "viewKnowledge": true,
  "viewAnalysis": true,
  "headerGlobalSearch": true,
  "chatReferencesEnabled": true,
  "chatReferencesModelContext": true,
  "chatReferencesSidebarIndicator": true,
  "inlineChatEnabled": true,
  "inlineChatModelContext": true,
  "inlineChatModelSchema": true,
  "inlineChatModelTable": true,
  "inlineChatModelColumn": true,
  "relatedContentEnabled": true,
  "relatedContentModelContext": true,
  "relatedContentModelSchema": true,
  "relatedContentModelTable": true,
  "relatedContentModelColumn": true
}
```

Omitted keys continue to use `defaultFeatureFlags` (see tables above).

### Error behavior

If `GET /api/v1/features` fails (network error, 401, 500), all flags fall back to `defaultFeatureFlags`. The application starts with the built-in defaults.

### Test coverage

- `src/features/__tests__/defaults.test.ts` — validates flag structure and defaults
- `src/features/__tests__/FeatureFlagContext.test.tsx` — validates merge with remote, error fallback, unknown key handling

---

*Last updated: April 2, 2026*
