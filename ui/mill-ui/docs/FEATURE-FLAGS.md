# Feature Flags Inventory

Complete inventory of all feature flags in the application. All flags are boolean, default to `true`, and are defined in `src/features/defaults.ts`.

---

## Architecture

- **Definition**: `src/features/defaults.ts` — `FeatureFlags` interface + `defaultFeatureFlags` object
- **Provider**: `src/features/FeatureFlagContext.tsx` — `FeatureFlagProvider` wraps the app
- **Hook**: `useFeatureFlags()` — returns the resolved `FeatureFlags` object
- **Backend**: `GET /api/v1/features` — returns `Partial<FeatureFlags>`. Omitted keys default to `true`. If the request fails, all flags default to `true`.
- **Total flags**: 70 across 14 categories

---

## Flag Inventory

### 1. Views (5 flags)

Top-level navigation routes. Disabling a flag hides the route and its nav button.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewHome` | `true` | Home / Overview dashboard route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewModel` | `true` | Data Model explorer route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewKnowledge` | `true` | Knowledge / Concepts route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewAnalysis` | `true` | Analysis / Query Playground route and nav button | `App.tsx`, `AppHeader.tsx` |
| `viewChat` | `true` | General Chat route and nav button | `App.tsx`, `AppHeader.tsx` |

### 2. Chat References (5 flags)

Control the "related conversations" feature — badges and popovers linking context objects to existing chat conversations.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `chatReferencesEnabled` | `true` | Master toggle for all chat reference features | `ChatReferencesContext.tsx`, `InlineChatDrawer.tsx`, `SchemaTree.tsx`, `ContextSidebar.tsx` |
| `chatReferencesModelContext` | `true` | Chat references on Data Model entities | `ChatReferencesContext.tsx` |
| `chatReferencesKnowledgeContext` | `true` | Chat references on Knowledge concepts | `ChatReferencesContext.tsx` |
| `chatReferencesAnalysisContext` | `true` | Chat references on Analysis queries | `ChatReferencesContext.tsx` |
| `chatReferencesSidebarIndicator` | `true` | Chat reference count badges in sidebar tree items | `SchemaTree.tsx`, `ContextSidebar.tsx` |

### 3. Inline Chat (10 flags)

Control the context-aware inline chat drawer that appears on detail pages.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `inlineChatEnabled` | `true` | Master toggle — hides the entire InlineChatDrawer | `App.tsx`, `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelContext` | `true` | Allow inline chat on Data Model entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelSchema` | `true` | Allow inline chat on SCHEMA-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelTable` | `true` | Allow inline chat on TABLE-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatModelColumn` | `true` | Allow inline chat on ATTRIBUTE-level entities | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatKnowledgeContext` | `true` | Allow inline chat on Knowledge concepts | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatAnalysisContext` | `true` | Allow inline chat on Analysis queries | `InlineChatButton.tsx`, `InlineChatContext.tsx` |
| `inlineChatMultiSession` | `true` | Allow multiple simultaneous inline chat sessions. When `false`, opening a new context replaces the existing session. | `InlineChatContext.tsx`, `InlineChatDrawer.tsx` |
| `inlineChatSessionGrouping` | `true` | Group inline sessions by route context type in the drawer tab bar | `InlineChatDrawer.tsx` |
| `inlineChatGreeting` | `true` | Show welcome/greeting message when a new inline chat session starts | `InlineChatContext.tsx` |

### 4. Model View Details (5 flags)

Control which facet tabs and UI elements appear on the Entity Details panel.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `modelDescriptiveFacet` | `true` | Descriptive facet tab (display name, description, business meaning, tags, synonyms) | `EntityDetails.tsx` |
| `modelStructuralFacet` | `true` | Structural facet tab (physical type, precision, PK/FK/unique/nullable) | `EntityDetails.tsx` |
| `modelRelationsFacet` | `true` | Relations facet tab (foreign keys, logical relationships) | `EntityDetails.tsx` |
| `modelQuickBadges` | `true` | Quick info badges below entity header (PK, FK, nullable, unique) | `EntityDetails.tsx` |
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
| `viewConnect` | `true` | Connect route and nav button | `App.tsx`, `AppHeader.tsx` |
| `connectServices` | `true` | Services connection guide section | `ConnectLayout.tsx` |
| `connectPython` | `true` | Python connection guide section | `ConnectLayout.tsx` |
| `connectJava` | `true` | Java connection guide section | `ConnectLayout.tsx` |

### 9. Admin View (5 flags)

Control the Admin panel and its sub-sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewAdmin` | `true` | Admin route and nav button | `App.tsx`, `AppHeader.tsx`, `OverviewDashboard.tsx` |
| `adminDataSources` | `true` | Data Sources management section | `AdminLayout.tsx` |
| `adminPolicies` | `true` | Policies management section | `AdminLayout.tsx` |
| `adminServices` | `true` | Services management section | `AdminLayout.tsx` |
| `adminSettings` | `true` | Settings management section | `AdminLayout.tsx` |

### 10. Profile View (4 flags)

Control the Profile view and its sub-sections.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `viewProfile` | `true` | Profile route and menu item in user dropdown | `App.tsx`, `AppHeader.tsx` |
| `profileGeneral` | `true` | General profile section | `ProfileLayout.tsx` |
| `profileSettings` | `true` | Settings section | `ProfileLayout.tsx` |
| `profileAccess` | `true` | Access / API keys section | `ProfileLayout.tsx` |

### 11. Login Providers (6 flags)

Control which OAuth/SSO buttons appear on the login page.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `loginGithub` | `true` | "Continue with GitHub" button | `LoginPage.tsx` |
| `loginGoogle` | `true` | "Continue with Google" button | `LoginPage.tsx` |
| `loginMicrosoft` | `true` | "Continue with Microsoft" button | `LoginPage.tsx` |
| `loginAws` | `true` | "Continue with AWS" button | `LoginPage.tsx` |
| `loginAzure` | `true` | "Continue with Azure AD" button | `LoginPage.tsx` |
| `loginPassword` | `true` | Email/password form on login page | `LoginPage.tsx` |

### 12. Related Content (8 flags)

Control the "related content" feature — pills/popovers linking to related schema entities, concepts, and queries.

| Flag | Default | What it controls | Consumer(s) |
|------|---------|-----------------|-------------|
| `relatedContentEnabled` | `true` | Master toggle for all related content features | `RelatedContentContext.tsx`, `RelatedContentButton.tsx`, `InlineChatDrawer.tsx` |
| `relatedContentModelContext` | `true` | Related content on Data Model entities | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentModelSchema` | `true` | Related content on SCHEMA-level entities | `RelatedContentButton.tsx` |
| `relatedContentModelTable` | `true` | Related content on TABLE-level entities | `RelatedContentButton.tsx` |
| `relatedContentModelColumn` | `true` | Related content on ATTRIBUTE-level entities | `RelatedContentButton.tsx` |
| `relatedContentKnowledgeContext` | `true` | Related content on Knowledge concepts | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentAnalysisContext` | `true` | Related content on Analysis queries | `RelatedContentButton.tsx`, `RelatedContentContext.tsx` |
| `relatedContentInDrawer` | `true` | Show related content section inside the inline chat drawer | `InlineChatDrawer.tsx` |

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
| `headerGlobalSearch` | `true` | Search icon + floating search overlay in header nav | `AppHeader.tsx` |
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

The backend returns `Partial<FeatureFlags>` — only flags it wants to override. Omitted flags default to `true`.

### Example: disable chat and admin

```json
{
  "viewChat": false,
  "viewAdmin": false,
  "inlineChatEnabled": false
}
```

This disables the Chat route, Admin route, and all inline chat features. All other features remain enabled.

### Error behavior

If `GET /api/v1/features` fails (network error, 401, 500), all flags default to `true`. The application starts with all features enabled.

### Test coverage

- `src/features/__tests__/defaults.test.ts` — validates flag structure and defaults
- `src/features/__tests__/FeatureFlagContext.test.tsx` — validates merge with remote, error fallback, unknown key handling

---

*Last updated: February 12, 2026*
