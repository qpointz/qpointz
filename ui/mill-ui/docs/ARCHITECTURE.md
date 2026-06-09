# DataChat - Architecture & Implementation Guide

## Overview

DataChat is a modern AI chat interface with integrated data model exploration, business context browsing, and SQL analytics. Built with React 19, TypeScript, Vite, and Mantine UI v8. The application features a comprehensive feature flag system, mock authentication, multiple theme support, and a full test suite.

**Live URL:** http://localhost:5173/

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [Application Architecture](#application-architecture)
4. [Authentication](#authentication)
5. [Feature Flags](#feature-flags)
6. [Views & Features](#views--features)
7. [Inline Context Chats](#inline-context-chats)
8. [Related Content](#related-content-cross-object-relationships)
9. [Shared Components](#shared-components)
10. [Design System](#design-system)
11. [State Management](#state-management)
12. [Routing](#routing)
13. [Data Layer](#data-layer)
14. [Component Documentation](#component-documentation)
15. [Testing](#testing)
16. [Future Backend Integration](#future-backend-integration)
17. [Development Commands](#development-commands)

---

## Tech Stack

### Core
| Package | Version | Purpose |
|---------|---------|---------|
| React | ^19.1.0 | UI framework |
| TypeScript | ~5.8.3 | Type safety |
| Vite | ^7.0.4 | Build tool & dev server |

### UI & Styling
| Package | Version | Purpose |
|---------|---------|---------|
| @mantine/core | ^8.1.3 | Component library |
| @mantine/hooks | ^8.1.3 | React hooks |
| @mantine/notifications | ^8.1.3 | Toast notifications |
| @mantine/code-highlight | ^8.1.3 | Syntax highlighting |
| react-icons | ^5.5.0 | Icon library (Heroicons) |

### Content Rendering
| Package | Version | Purpose |
|---------|---------|---------|
| react-markdown | ^10.1.0 | Markdown rendering in chat |
| shiki | ^3.8.0 | Code syntax highlighting |
| sql-formatter | ^15.6.6 | SQL formatting |
| mermaid | ^11.8.1 | Diagram rendering (available) |

### Data & Routing
| Package | Version | Purpose |
|---------|---------|---------|
| react-router | ^7.6.3 | Client-side routing |
| @tanstack/react-table | ^8.21.3 | Query results table |
| axios | ^1.10.0 | HTTP client (for future API) |
| echarts-for-react | ^3.0.2 | Charts (available) |

### Testing
| Package | Version | Purpose |
|---------|---------|---------|
| vitest | ^4.0.16 | Test runner |
| @testing-library/react | ^16.3.1 | Component testing |
| @testing-library/jest-dom | ^6.9.1 | DOM matchers |
| @testing-library/user-event | ^14.6.1 | User interaction simulation |
| jsdom | ^27.3.0 | Browser environment |

---

## Project Structure

```
src/
├── App.tsx                        # Root: auth context, providers, routing
├── main.tsx                       # Entry point with BrowserRouter
├── index.css                      # Global styles
│
├── components/
│   ├── __tests__/                 # Component test files (16 tests)
│   │
│   ├── layout/                    # App shell & navigation
│   │   ├── AppHeader.tsx          # Top nav bar: tabs, admin, user menu, logout
│   │   ├── AppShell.tsx           # Chat view container with sidebar
│   │   └── Sidebar.tsx            # Chat conversation list with delete confirmation
│   │
│   ├── chat/                      # Chat feature components
│   │   ├── ChatArea.tsx           # Main chat container (title + messages + input)
│   │   ├── MessageList.tsx        # Scrollable message container with empty state
│   │   ├── MessageBubble.tsx      # Individual message with markdown rendering
│   │   ├── MessageInput.tsx       # Auto-resize textarea with send button
│   │   ├── TypingIndicator.tsx    # AI typing animation (3 dots)
│   │   └── TypingIndicator.css    # Bounce keyframe animation
│   │
│   ├── inline-chat/               # Inline context-aware chat components
│   │   ├── InlineChatDrawer.tsx   # Right-side drawer with session tabs
│   │   ├── InlineChatPanel.tsx    # Single chat: context banner + messages + input
│   │   ├── InlineChatMessage.tsx  # Compact message bubble with markdown
│   │   └── InlineChatInput.tsx    # Single-line input with send icon
│   │
│   ├── common/                    # Shared / reusable components
│   │   ├── CollapsibleSidebar.tsx # Collapsible sidebar wrapper (used by all views)
│   │   ├── InlineChatButton.tsx   # Context-aware inline chat trigger button
│   │   ├── RelatedContentButton.tsx # Cross-object related content trigger button
│   │   ├── RelatedModelTree.tsx   # Hierarchical tree for related model entities
│   │   ├── CodeBlock.tsx          # Syntax highlighted code block
│   │   ├── NotFoundPage.tsx       # 404 error page
│   │   └── AccessDeniedPage.tsx   # 403 error page
│   │
│   ├── overview/                  # Overview Dashboard view
│   │   └── OverviewDashboard.tsx  # Stats cards + quick links
│   │
│   ├── data-model/                # Data Model Explorer view
│   │   ├── DataModelLayout.tsx    # Main layout with sidebar + content
│   │   ├── SchemaTree.tsx         # Expandable tree with chat indicators
│   │   ├── EntityDetails.tsx      # Selected entity with tabs + RelatedContentButton + InlineChatButton
│   │   └── facets/                # Facet viewer components (Model view uses standard descriptor UI;
│   │       │                        # DescriptiveFacet / RelationFacet are legacy and unused there)
│   │       ├── DescriptiveFacet.tsx   # Legacy presentational component (tests only)
│   │       ├── StructuralFacet.tsx    # Structural read view in EntityDetails
│   │       └── RelationFacet.tsx      # Legacy presentational component (tests only)
│   │
│   ├── context/                   # Context (Knowledge) Explorer view
│   │   ├── ContextLayout.tsx      # Main layout with sidebar + content
│   │   ├── ContextSidebar.tsx     # Categories, tags, concept list
│   │   └── ConceptDetails.tsx     # Selected concept + RelatedContentButton + InlineChatButton
│   │
│   ├── queries/                   # Query Playground (Analysis) view
│   │   ├── QueryPlayground.tsx    # Main layout: sidebar + editor + results
│   │   ├── QuerySidebar.tsx       # Query list with new/delete actions
│   │   ├── QueryEditor.tsx        # SQL editor toolbar + header + InlineChatButton
│   │   ├── SqlCodeEditor.tsx      # CodeMirror 6 SQL surface (schema completions)
│   │   ├── schemaCompletionIndex.ts # Tree → completion labels (schemas, tables, columns)
│   │   └── QueryResults.tsx       # Sortable table with export dropdown
│   │
│   ├── admin/                     # Admin view
│   │   └── AdminLayout.tsx        # Sidebar nav: Data Sources, Policies, Services, Settings
│   │
│   ├── profile/                   # User Profile view
│   │   └── ProfileLayout.tsx      # Sidebar nav: General, Settings, Access
│   │
│   └── auth/                      # Authentication
│       └── LoginPage.tsx          # Login page with social + password options
│
├── context/                       # React Context providers
│   ├── __tests__/                 # Context tests (4 files)
│   ├── ChatContext.tsx            # Chat state (useReducer + localStorage)
│   ├── InlineChatContext.tsx      # Inline chat state + listener registry
│   ├── ChatReferencesContext.tsx  # Related general-chat references (cache + hooks)
│   └── RelatedContentContext.tsx  # Cross-object related content references (cache + hooks)
│
├── data/                          # Mock data (static + localStorage simulation)
│   ├── __tests__/                 # Data tests (3 files)
│   ├── mockSchema.ts             # Schema tree + facets data
│   └── mockConcepts.ts           # Business concepts data
│
├── features/                      # Feature flag system
│   ├── __tests__/                 # Feature flag tests (1 file)
│   ├── defaults.ts               # FeatureFlags interface + defaultFeatureFlags
│   └── FeatureFlagContext.tsx     # Provider + useFeatureFlags hook
│
├── services/                      # Centralized API layer (one file per service)
│   ├── api.ts                    # Barrel: re-exports all 9 services (single import point)
│   ├── schemaService.ts          # SchemaService -- tree, entity lookup, facets
│   ├── conceptService.ts         # ConceptService -- concepts, categories, tags
│   ├── queryService.ts           # QueryService -- execute SQL, saved queries
│   ├── statsService.ts           # StatsService -- dashboard aggregate counts
│   ├── chatService.ts            # ChatService -- streaming chat messages
│   ├── inlineChatService.ts      # InlineChatService -- context-aware streaming chat
│   ├── chatReferencesService.ts  # ChatReferencesService -- related conversations
│   ├── relatedContentService.ts  # RelatedContentService -- cross-object relationships
│   ├── featureService.ts         # FeatureFlagService -- remote feature flags
│   └── mockApi.ts                # (legacy) Original monolithic mock -- superseded by per-file services
│
├── test/                          # Test infrastructure
│   └── setup.ts                  # jsdom polyfills (matchMedia, ResizeObserver, scrollTo)
│
├── theme/                         # Mantine theme + color theme system
│   ├── theme.ts                  # buildTheme() — colors, fonts, component defaults
│   ├── themes.ts                 # Light/dark theme palette definitions
│   └── ThemeContext.tsx           # ColorThemeProvider + useColorTheme hook
│
└── types/                         # TypeScript type definitions
    ├── chat.ts                   # Message, Conversation, ChatState, ChatService
    ├── chatReferences.ts         # ConversationRef, ChatReferencesService
    ├── relatedContent.ts         # RelatedContentRef, RelatedContentType, RelatedContentService
    ├── inlineChat.ts             # InlineChatSession, InlineChatState, InlineChatAction
    ├── schema.ts                 # SchemaEntity, EntityFacets, DescriptiveFacet, SchemaService
    ├── context.ts                # Concept, ConceptFilter, ConceptService
    ├── query.ts                  # SavedQuery, QueryResult, QueryColumn, QueryService
    └── stats.ts                  # DashboardStats, StatsService
```

---

## Application Architecture

### Provider Hierarchy

```
App
└── AuthContext.Provider                    (mock auth: isAuthenticated, login, logout)
    └── ColorThemeProvider                  (light/dark theme selection, localStorage)
        └── FeatureFlagProvider             (remote-fetched + default feature flags)
            └── ThemedApp                   (MantineProvider with dynamic theme)
                ├── [if !authenticated] → LoginPage
                └── [if authenticated]
                    └── InlineChatProvider  (ephemeral chat sessions, listener registry)
                        └── ChatReferencesProvider  (related general-chat refs, cache)
                            └── RelatedContentProvider  (cross-object relationship refs, cache)
                                ├── Notifications
                                ├── AppHeader      (navigation, admin tab, user menu)
                                ├── Routes         (feature-flag gated)
                                └── InlineChatDrawer (conditional right drawer)
```

### Component Hierarchy

```
App
├── AuthContext.Provider
│   └── ColorThemeProvider
│       └── FeatureFlagProvider
│           └── ThemedApp (MantineProvider)
│               ├── LoginPage (when not authenticated)
│               └── InlineChatProvider (when authenticated)
│                   ├── AppHeader
│                   │   ├── Logo + Brand
│                   │   ├── Tabs (left-aligned): Overview, Model, Knowledge, Analysis, Chat
│                   │   ├── Tabs (right-aligned): Admin
│                   │   └── Actions: User Menu (Profile, Log out)
│                   │
│                   ├── Routes (flex: 1)
│                   │   ├── /overview → OverviewDashboard
│                   │   │   ├── Stat cards (Schemas, Tables, Concepts, Queries)
│                   │   │   └── Quick links (feature-flag gated)
│                   │   │
│                   │   ├── /chat, /chat/:conversationId → ChatView (nested)
│                   │   │   └── ChatProvider
│                   │   │       └── AppShell
│                   │   │           ├── CollapsibleSidebar → Sidebar (conversation list)
│                   │   │           └── ChatArea
│                   │   │               ├── MessageList → MessageBubble (multiple)
│                   │   │               └── MessageInput
│                   │   │
│                   │   ├── /model/* → DataModelLayout
│                   │   │   ├── CollapsibleSidebar → SchemaTree (recursive TreeNode)
│                   │   │   └── EntityDetails + RelatedContentButton + InlineChatButton
│                   │   │       └── Tabs: Descriptive | Structural | Relations
│                   │   │
│                   │   ├── /knowledge/* → ContextLayout
│                   │   │   ├── CollapsibleSidebar → ContextSidebar
│                   │   │   └── ConceptDetails + RelatedContentButton + InlineChatButton
│                   │   │
│                   │   ├── /analysis/* → QueryPlayground
│                   │   │   ├── CollapsibleSidebar → QuerySidebar (new/delete)
│                   │   │   └── QueryEditor + InlineChatButton
│                   │   │       └── QueryResults (sortable table + export)
│                   │   │
│                   │   ├── /admin/* → AdminLayout
│                   │   │   └── CollapsibleSidebar → NavLinks (Data Sources, etc.)
│                   │   │
│                   │   ├── /profile/* → ProfileLayout
│                   │   │   └── CollapsibleSidebar → NavLinks (General, Settings, Access)
│                   │   │
│                   │   └── /* → NotFoundPage (404)
│                   │
│                   └── InlineChatDrawer (right side, 380px, conditional)
│                       ├── Session list popover
│                       └── InlineChatPanel → InlineChatMessage + InlineChatInput
```

---

## Authentication

### Mock Auth Context

**Location:** `src/App.tsx`

The application implements a lightweight mock authentication system:

```typescript
interface AuthContextValue {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
}
```

- **Default state:** `isAuthenticated = true` (starts authenticated for development)
- **Login flow:** `LoginPage` calls `onLogin` → `setIsAuthenticated(true)` → app shell renders
- **Logout flow:** User menu "Log out" calls `logout()` → `setIsAuthenticated(false)` → `LoginPage` renders
- **Conditional rendering:** When not authenticated, only `LoginPage` is shown (no header, no navigation)

### Login Page

**File:** `src/components/auth/LoginPage.tsx`

Full-screen login dialog with:
- DataChat brand identity
- Social login providers (GitHub, Google, Microsoft, AWS, Azure) — each gated by feature flags
- Email/password form (gated by `loginPassword` flag)
- Custom inline SVG icons for each provider
- Dark mode gradient background

---

## Feature Flags

### System Design

**Files:** `src/features/defaults.ts`, `src/features/FeatureFlagContext.tsx`

Feature flags control visibility of every major UI element. The system supports remote flag fetching with local defaults as fallback.

### Provider

```typescript
export function FeatureFlagProvider({ children }) {
  const [flags, setFlags] = useState(defaultFeatureFlags);
  useEffect(() => {
    featureService.getFlags().then((remote) => {
      setFlags((prev) => ({ ...prev, ...remote }));  // Remote overrides defaults
    }).catch(() => { /* silent fallback */ });
  }, []);
  return <FeatureFlagContext.Provider value={flags}>{children}</FeatureFlagContext.Provider>;
}
```

### Flag Categories

| Category | Flags | Controls |
|----------|-------|----------|
| **Views** | `viewOverview`, `viewModel`, `viewKnowledge`, `viewAnalysis`, `viewChat`, `viewAdmin`, `viewProfile` | Top-level route visibility |
| **Chat References** | `chatReferencesEnabled`, `chatReferencesModelContext`, `chatReferencesKnowledgeContext`, `chatReferencesAnalysisContext`, `chatReferencesSidebarIndicator` | Related general-chat indicators |
| **Inline Chat** | `inlineChatEnabled`, `inlineChatGreeting`, `inlineChatModelContext`, `inlineChatModelSchema`, `inlineChatModelTable`, `inlineChatModelColumn`, `inlineChatKnowledgeContext`, `inlineChatAnalysisContext` | Inline chat per context type |
| **Related Content** | `relatedContentEnabled`, `relatedContentModelContext`, `relatedContentModelSchema`, `relatedContentModelTable`, `relatedContentModelColumn`, `relatedContentKnowledgeContext`, `relatedContentAnalysisContext`, `relatedContentInDrawer` | Cross-object relationship indicators |
| **Model View** | `modelDescriptiveFacet`, `modelStructuralFacet`, `modelRelationsFacet`, `modelQuickBadges`, `modelPhysicalType` | Entity detail sections |
| **Knowledge View** | `knowledgeDescription`, `knowledgeTags`, `knowledgeSqlDefinition`, `knowledgeRelatedEntities`, `knowledgeMetadata`, `knowledgeSourceBadge` | Concept detail sections |
| **Analysis View** | `analysisSqlEditor`, `analysisResults`, `analysisFormatSql`, `analysisCopySql`, `analysisClearSql`, `analysisExport` | Editor toolbar actions |
| **Sidebar** | `sidebarCollapsible`, `sidebarModelBadge`, `sidebarKnowledgeBadge`, `sidebarKnowledgeCategories`, `sidebarKnowledgeTags`, `sidebarAnalysisBadge` | Sidebar behavior |
| **Admin** | `adminDataSources`, `adminPolicies`, `adminServices`, `adminSettings` | Admin subsections |
| **Profile** | `profileGeneral`, `profileSettings`, `profileAccess` | Profile subsections |
| **Login** | `loginGithub`, `loginGoogle`, `loginMicrosoft`, `loginAws`, `loginAzure`, `loginAuthentik`, `loginPassword` | Login provider buttons |
| **Header** | `headerThemeSwitcher`, `headerUserProfile` | Header UI elements |

All flags default to `true` for development.

---

## Views & Features

### 1. Overview Dashboard (`/overview`)

**Purpose:** Landing page with statistics and quick navigation

**Features:**
- Stat cards: Schemas, Tables, Concepts, Queries (computed from mock data)
- Quick links to all major views (Model, Knowledge, Analysis, Chat, Admin)
- Admin quick link conditionally shown based on `viewAdmin` flag
- Clickable stat cards navigate to relevant views

### 2. Chat View (`/chat`, `/chat/:conversationId`)

**Purpose:** AI chat interface with conversation management

**URL pattern:** index **`/chat`** and optional **`/chat/:conversationId`** where **`conversationId`** is the server chat UUID (`POST /api/v1/ai/chats`). The app uses **`basename=/app`**, so deployed paths are under `/app/chat/...`. **`ChatRouteSync`** keeps the address bar and `ChatContext` in sync.

**Features:**
- Conversation sidebar with history (CollapsibleSidebar)
- New chat creation (+ button in sidebar header)
- Delete conversations with popover confirmation
- Message bubbles (user right-aligned, AI left-aligned)
- Markdown rendering with syntax highlighting (ReactMarkdown + CodeBlock)
- Streaming AI responses (simulated via AsyncGenerator)
- Typing indicator animation (3 bouncing dots)
- Auto-scroll to latest message
- LocalStorage persistence
- Auto-generates conversation title from first user message

**State Shape:**
```typescript
interface ChatState {
  conversations: Conversation[];
  activeConversationId: string | null;
  isLoading: boolean;
}
```

### 3. Model View (`/model`)

**Purpose:** Browse database schema with metadata facets

**URL Pattern:** `/model/:schema?/:table?/:attribute?`

**Features:**
- Expandable tree navigation (Schema → Table → Column) with chat indicators
- Entity header with type badge, constraints, RelatedContentButton, and InlineChatButton
- Tabbed facet display (Descriptive, Structural, Relations) — each flag-gated
- URL synchronization for deep linking
- CollapsibleSidebar with "Schema Browser" title

### 4. Knowledge View (`/knowledge`)

**Purpose:** Browse business concepts / definitions

**URL Pattern:** `/knowledge/:conceptId?`

**Features:**
- Category filter with counts
- Tag filter with counts
- Filtered concept list with chat indicators
- Concept details with RelatedContentButton and InlineChatButton: name, category, source badge, description, tags, SQL definition, related entities
- Click category/tag toggles filter (click again to clear)

### 5. Query Playground (`/analysis`)

**Purpose:** Interactive SQL workspace

**URL Pattern:** `/analysis/:queryId?`

**Features:**
- **Sidebar:** Sample queries list + "New Query" button + per-query delete with confirmation
- **Editor header:** Dynamic title/description from selected query, InlineChatButton
- **SQL editor:** Monospace textarea with Format, Copy, Clear actions on the execute bar
- **Execute:** Button or Ctrl+Enter keyboard shortcut
- **Results table:** Sortable columns (@tanstack/react-table), type badges, number formatting, NULL styling
- **Export dropdown:** CSV, Excel (TSV), JSON download
- **Status bar:** Row count and execution time
- **AI integration:** `useInlineChatListener` extracts SQL from AI responses and populates the editor
- User-created queries (dynamic state, not persisted)

### 6. Admin View (`/admin`)

**Purpose:** Administrative area for platform management

**URL Pattern:** `/admin/:section?`

**Features:**
- CollapsibleSidebar with gear icon and "Admin" title
- Navigation: Data Sources, Policies, Services, Settings — each gated by feature flags
- Placeholder panels per section
- Separated in header navigation (right-aligned tab)

### 7. User Profile (`/profile`)

**Purpose:** User account and preference management

**URL Pattern:** `/profile/:section?`

**Features:**
- CollapsibleSidebar with user icon and "Profile" title
- User identity card at top of sidebar (avatar, name, email)
- Navigation: General, Settings, Access — each gated by feature flags
- Placeholder panels per section

### 8. Error Pages

**Not Found (404):** `src/components/common/NotFoundPage.tsx`
- Centered layout with warning icon, "404", "Page not found"
- "Go to Overview" and "Go back" buttons
- Customizable message via `message` prop

**Access Denied (403):** `src/components/common/AccessDeniedPage.tsx`
- Centered layout with shield icon, "403", "Access denied"
- "Go to Overview" and "Go back" buttons
- Customizable message via `message` prop

---

## Inline Context Chats

### Overview

Inline context chats are lightweight, ephemeral AI assistant sessions that can be started from the Model, Knowledge, and Analysis views. Each view embeds an `InlineChatButton` in its header, making the chat entry point contextually relevant.

### How It Works

1. **Starting a chat:** The `InlineChatButton` component (placed in view-specific headers) starts a new context-aware chat session. The button is feature-flag gated at both global and per-context levels.
2. **Drawer:** The chat opens in a right-side drawer (380px wide) that sits alongside the main content.
3. **Multiple sessions:** Users can have multiple context-aware chats open simultaneously and switch between them.
4. **Ephemeral lifecycle:** Sessions are not persisted — they exist in React state only.
5. **Visual indicators:** Sidebar items show a small chat icon next to items with active sessions. The InlineChatButton shows a notification dot when an active session exists for the current context.

### Listener System

The `InlineChatContext` provides a listener registry that allows views to react to AI responses:

```typescript
// Register a listener for a specific context
registerListener(contextId: string, callback: (content: string) => void)
unregisterListener(contextId: string, callback)

// Convenience hook
useInlineChatListener(contextId, onAssistantMessage)
```

**Use case:** The Analysis view uses `useInlineChatListener` to extract SQL from AI responses and auto-populate the query editor.

### InlineChatButton

**File:** `src/components/common/InlineChatButton.tsx`

Reusable button placed in view-specific headers (EntityDetails, ConceptDetails, QueryEditor).

**Feature flag checks (in render order):**
1. `inlineChatEnabled` (global)
2. Per context type: `inlineChatModelContext`, `inlineChatKnowledgeContext`, `inlineChatAnalysisContext`
3. Per entity type (model only): `inlineChatModelSchema`, `inlineChatModelTable`, `inlineChatModelColumn`

### Session State

```typescript
interface InlineChatSession {
  id: string;
  contextType: 'model' | 'knowledge' | 'analysis';
  contextId: string;
  contextLabel: string;
  contextEntityType?: string;
  messages: Message[];
  isLoading: boolean;
  createdAt: number;
}
```

---

## Chat References (Related General Conversations)

### Overview

Chat References surface awareness that an object (table, concept, query) was referenced in general Chat conversations. The feature integrates into the existing inline chat UX — the `InlineChatButton` and `InlineChatDrawer` — rather than adding separate indicators.

### How It Works

1. **Service call:** A `ChatReferencesService` (`src/types/chatReferences.ts`) provides `getConversationsForContext(contextType, contextId)` returning `ConversationRef[]` (id + title). Currently mocked in `src/services/mockApi.ts`.
2. **Context/Cache:** `ChatReferencesProvider` (`src/context/ChatReferencesContext.tsx`) caches results in a `Map` and exposes `useChatReferences(contextType, contextId)` hook and synchronous `getRefsForContextId(contextId)` for sidebar items.
3. **Prefetch:** Layout components (`DataModelLayout`, `ContextLayout`) call `prefetchRefs()` on mount to batch-load references for all sidebar items.

### InlineChatButton Enhancement

The `InlineChatButton` now checks both inline chat sessions AND related general conversations:

| Inline session? | Related chats? | Button indicator | Click behavior |
|:---:|:---:|---|---|
| No | No | No indicator | Start inline chat (unchanged) |
| No | Yes | Violet badge with count | Show popover: "Start inline chat" + related conversation list |
| Yes | No | Red dot (unchanged) | Open drawer (unchanged) |
| Yes | Yes | Red dot + violet badge | Open drawer (unchanged); related chats visible in panel |

### InlineChatPanel Enhancement

When an inline chat session is active and related conversations exist, a collapsible "N related conversations" bar appears below the context banner. Each item navigates to the general Chat view.

### Sidebar Indicators

`SchemaTree` and `ContextSidebar` show a small violet count badge next to items that have related general conversations, alongside the existing teal/cyan inline chat icon.

### Feature Flags

| Flag | Default | Controls |
|------|---------|----------|
| `chatReferencesEnabled` | `true` | Global kill-switch |
| `chatReferencesModelContext` | `true` | Model view entities |
| `chatReferencesKnowledgeContext` | `true` | Knowledge view concepts |
| `chatReferencesAnalysisContext` | `true` | Analysis view queries |
| `chatReferencesSidebarIndicator` | `true` | Sidebar count badges |

### Type

```typescript
interface ConversationRef {
  id: string;
  title: string;
}
```

---

## Related Content (Cross-Object Relationships)

### Overview

Related Content surfaces bidirectional relationships between objects across domains: Model entities (schema, table, column) can relate to Knowledge concepts, Analysis queries, and other model entities. Concepts can relate to model entities and other concepts. The feature follows the same architectural pattern as Chat References but uses distinct visual treatment.

### Relationship Model

```
Model (Schema/Table/Column) ↔ Knowledge (Concept)
Model (Schema/Table/Column) ↔ Analysis (Query)
Concept ↔ Concept
```

All relationships are bidirectional. The backend always provides the **full model hierarchy** — if a column is related, its parent table and schema are included in the response.

### How It Works

1. **Service call:** `RelatedContentService` (`src/types/relatedContent.ts`) provides `getRelatedContent(contextType, contextId)` returning `RelatedContentRef[]`. Each ref has `id`, `title`, `type` (`'model' | 'concept' | 'analysis'`), and optional `entityType`.
2. **Context/Cache:** `RelatedContentProvider` (`src/context/RelatedContentContext.tsx`) caches results and exposes `useRelatedContent(contextType, contextId)` hook.
3. **Feature flags:** Gated at global, per-context, and per-entity-type levels (mirrors inline chat flag structure).

### UI Components

#### RelatedContentButton

**File:** `src/components/common/RelatedContentButton.tsx`

Placed **left of** the `InlineChatButton` in `EntityDetails` and `ConceptDetails` headers. Uses **indigo** color scheme to differentiate from the chat button.

- Shows an indigo link icon with count badge when related content exists
- Click opens a popover with:
  - **Model refs** rendered as a hierarchical tree (schema → table → column)
  - **Concept/Analysis refs** rendered as a flat list with colored type badges (grape for concept, orange for analysis)
- Clicking any item navigates to the relevant view

#### RelatedModelTree

**File:** `src/components/common/RelatedModelTree.tsx`

Shared tree component used by both the `RelatedContentButton` popover and the `InlineChatDrawer` related content popover.

**Tree hierarchy rules:**
| Level | Node | Expand behavior | Chevron |
|-------|------|----------------|---------|
| Schema (depth 0) | Always expanded | No chevron |
| Table (depth 1) | Expanded if ≤3 columns; collapsed if >3 | Chevron only when >3 children |
| Column (depth 2+) | Leaf node | None |

- Minimal per-level indent (~6px step) for visual hierarchy without wasting space
- Schema-level alignment matches non-model flat list items
- Compact mode for smaller spaces (drawer popover)
- Each navigable node has an arrow icon; clicking navigates to `/model/{path}`

#### InlineChatDrawer Enhancement

The drawer header shows a related content popover (indigo link icon + badge) **left of** the existing related chats popover (violet), when `relatedContentInDrawer` flag is enabled. Same tree + flat list rendering as the button popover, in compact mode.

### Visual Color Scheme

| Feature | Color |
|---------|-------|
| Inline chat sessions | cyan/teal |
| Related chats (Chat References) | violet |
| Related content button/badge | **indigo** |
| Model type badge | teal |
| Concept type badge | grape |
| Analysis type badge | orange |

### Feature Flags

| Flag | Default | Controls |
|------|---------|----------|
| `relatedContentEnabled` | `true` | Global kill-switch |
| `relatedContentModelContext` | `true` | Model view entities |
| `relatedContentModelSchema` | `true` | Schema-level entities |
| `relatedContentModelTable` | `true` | Table-level entities |
| `relatedContentModelColumn` | `true` | Column-level entities |
| `relatedContentKnowledgeContext` | `true` | Knowledge view concepts |
| `relatedContentAnalysisContext` | `true` | Analysis view queries |
| `relatedContentInDrawer` | `true` | Related content popover in InlineChatDrawer |

### Types

```typescript
type RelatedContentType = 'model' | 'concept' | 'analysis';

interface RelatedContentRef {
  id: string;
  title: string;
  type: RelatedContentType;
  entityType?: string;  // 'SCHEMA' | 'TABLE' | 'ATTRIBUTE' (model only)
}
```

---

## Shared Components

### CollapsibleSidebar

**File:** `src/components/common/CollapsibleSidebar.tsx`

Reusable sidebar wrapper used by all view layouts (Chat, Model, Knowledge, Analysis, Admin, Profile).

**Props:**
```typescript
interface CollapsibleSidebarProps {
  icon: React.ComponentType<{ size: number; color?: string }>;
  title: string;
  headerRight?: React.ReactNode;
  children: React.ReactNode;
  defaultCollapsed?: boolean;
}
```

**Features:**
- Width: 280px when expanded, 0px when collapsed (removed from flex layout)
- Collapse controlled by `sidebarCollapsible` feature flag
- When collapsed: uses `position: fixed` hover zone (24px) at left edge
- Fade-in pill button on hover to re-expand
- Header with icon, title, optional right-side content, and collapse toggle

### Error Pages

Both NotFoundPage and AccessDeniedPage follow the same centered layout pattern:
1. Large icon in circular colored background (80x80px)
2. HTTP status code (bold)
3. Title (semi-bold)
4. Descriptive message (customizable)
5. Action buttons: "Go to Overview" + "Go back"

---

## Design System

### Color Theme System

**Files:** `src/theme/themes.ts`, `src/theme/ThemeContext.tsx`, `src/theme/theme.ts`

The application supports multiple color themes that can be independently selected for light and dark modes.

**Architecture:**
- `themes.ts` defines theme palettes (Ocean, Forest, Lavender, Sunset, etc.)
- `ThemeContext.tsx` provides `ColorThemeProvider` with localStorage persistence
- `theme.ts` exports `buildTheme()` which creates a Mantine theme from selected palettes

**Theme selection persisted in localStorage:**
- Light theme: `mill-ui-light-theme`
- Dark theme: `mill-ui-dark-theme`

### Color Palette

**Primary Colors (Teal/Cyan):**
- Light mode accent: Teal (`#14b8a6`)
- Dark mode accent: Cyan (`#0891b2`)
- Neutral: Slate (blue-toned gray)

**Usage:**
| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| Primary buttons | `teal.6` | `cyan.6` |
| Active nav | `teal.1` bg | `cyan.9` bg |
| User message | `teal.6` bg | `cyan.7` bg |
| AI message | `gray.1` bg | `slate.7` bg |
| Sidebar | `slate.0/1` bg | `slate.9` bg |
| Main content | `white` bg | `slate.8` bg |

### Icon Usage

Using `react-icons/hi2` (Heroicons v2 Outline):

| Icon | Component | Usage |
|------|-----------|-------|
| Chat | `HiOutlineChatBubbleLeftRight` | Chat nav, messages |
| Model | `HiOutlineSquare3Stack3D` | Model nav |
| Knowledge | `HiOutlineAcademicCap` | Knowledge nav |
| Analysis | `HiOutlineBeaker` | Analysis nav |
| Overview | `HiOutlineSquares2X2` | Overview nav |
| Admin | `HiOutlineWrenchScrewdriver` | Admin nav |
| Schema | `HiOutlineCircleStack` | Schema/database entities |
| Table | `HiOutlineTableCells` | Table entities |
| Column | `HiOutlineViewColumns` | Column/attribute entities |
| Concept | `HiOutlineLightBulb` | Business concepts |
| Settings | `HiOutlineCog6Tooth` | Settings, admin |
| User | `HiOutlineUserCircle` | Profile, user menu |
| Lock | `HiOutlineLockClosed` | Access/permissions |
| Add | `HiOutlinePlus` | Create actions |
| Delete | `HiOutlineTrash` | Delete actions |
| Send | `HiPaperAirplane` | Send message |
| SQL | `HiOutlineCommandLine` | Query editor |
| Export | `HiOutlineArrowDownTray` | Export results |
| Logout | `HiOutlineArrowRightOnRectangle` | Log out |
| Warning | `HiOutlineExclamationTriangle` | 404 page |
| Shield | `HiOutlineShieldExclamation` | 403 page |

### Layout Patterns

**Master Layout:**
```
┌────────────────────────────────────────────────────────────────────┐
│ AppHeader (h=56px)                                                  │
│ [Logo]  [Overview][Model][Knowledge][Analysis][Chat]   [Admin] [👤] │
├────────────────────────────────────────────────────────────────────┤
│                                                 │                   │
│  View Content (flex: 1)                         │ InlineChatDrawer  │
│  ┌──────────┐ ┌────────────────────┐            │ (380px, cond.)    │
│  │ Collaps. │ │ Main Content       │            │                   │
│  │ Sidebar  │ │ (view-specific)    │            │                   │
│  │ (280px)  │ │                    │            │                   │
│  └──────────┘ └────────────────────┘            │                   │
└─────────────────────────────────────────────────┴───────────────────┘
```

**AppHeader navigation:**
- Main items (left-aligned): Overview, Model, Knowledge, Analysis, Chat
- Right items (right-aligned): Admin
- Actions (far right): User menu with Profile and Log out

---

## State Management

### Chat State (React Context + useReducer)

**Location:** `src/context/ChatContext.tsx`

**Actions:**
```typescript
type ChatAction =
  | { type: 'LOAD_CONVERSATIONS'; payload: Conversation[] }
  | { type: 'CREATE_CONVERSATION'; payload: Conversation }
  | { type: 'DELETE_CONVERSATION'; payload: string }
  | { type: 'SET_ACTIVE_CONVERSATION'; payload: string | null }
  | { type: 'ADD_MESSAGE'; payload: { conversationId: string; message: Message } }
  | { type: 'UPDATE_MESSAGE'; payload: { conversationId: string; messageId: string; content: string } }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'CLEAR_ALL' };
```

**Persistence:** LocalStorage with key `chat-conversations`
**Scope:** Wraps ChatView only (via `ChatProvider` inside `ChatView` component)

### Inline Chat State (React Context + useReducer)

**Location:** `src/context/InlineChatContext.tsx`

**Scope:** App-wide — `InlineChatProvider` wraps the entire authenticated shell

**Key Methods via `useInlineChat()`:**
- `startSession(contextType, contextId, contextLabel, contextEntityType?)`
- `closeSession(id)` / `closeAllSessions()`
- `setActiveSession(id)`
- `sendMessage(sessionId, content)`
- `openDrawer()` / `closeDrawer()`
- `getSessionByContextId(contextId)` — used for indicator icons
- `hasAnySessions()`
- `registerListener(contextId, callback)` / `unregisterListener(contextId, callback)`

**Listener system:** After streaming an AI response, callbacks registered for the session's `contextId` are invoked with the full response content. Used by the Analysis view to extract SQL.

### Feature Flag State

**Location:** `src/features/FeatureFlagContext.tsx`

**Pattern:** useState + useEffect for remote fetch
- Starts with `defaultFeatureFlags` (all `true`)
- Fetches from `featureService.getFlags()` on mount
- Merges remote into defaults (remote overrides)

### Auth State

**Location:** `src/App.tsx`

**Pattern:** useState + useCallback
- `isAuthenticated` boolean controls LoginPage vs app shell rendering
- `login()` / `logout()` callbacks

### Color Theme State

**Location:** `src/theme/ThemeContext.tsx`

**Pattern:** useState with localStorage persistence
- Independent light/dark theme selection
- Persists theme IDs to localStorage

---

## Routing

### Routes (Feature-Flag Gated)

**Location:** `src/App.tsx`

```typescript
<Routes>
  {flags.viewOverview  && <Route path="/overview" element={<OverviewDashboard />} />}
  {flags.viewModel     && <Route path="/model/:schema?/:table?/:attribute?" element={<DataModelLayout />} />}
  {flags.viewKnowledge && <Route path="/knowledge/:conceptId?" element={<ContextLayout />} />}
  {flags.viewAnalysis  && <Route path="/analysis/:queryId?" element={<QueryPlayground />} />}
  {flags.viewChat      && <Route path="/chat/*" element={<ChatView />} />}
  {flags.viewAdmin     && <Route path="/admin/:section?" element={<AdminLayout />} />}
  {flags.viewProfile   && <Route path="/profile/:section?" element={<ProfileLayout />} />}
  <Route index element={<Navigate to={defaultRoute} replace />} />
  <Route path="*" element={<NotFoundPage />} />
</Routes>
```

### URL Patterns

| View | Pattern | Example |
|------|---------|---------|
| Overview | `/overview` | `/overview` |
| Chat | `/chat`, `/chat/:conversationId` | `/chat` |
| Model | `/model/:schema?/:table?/:attribute?` | `/model/sales/customers/customer_id` |
| Knowledge | `/knowledge/:conceptId?` | `/knowledge/customer-lifetime-value` |
| Analysis | `/analysis/:queryId?` | `/analysis/top-customers` |
| Admin | `/admin/:section?` | `/admin/data-sources` |
| Profile | `/profile/:section?` | `/profile/settings` |

The default route dynamically resolves to the first enabled view (priority: overview → model → knowledge → analysis → chat).

---

## Data Layer

### Architecture: Centralized Service Barrel

All data access in the application flows through a single module: **`src/services/api.ts`**. This barrel re-exports every service (9 total) from its own dedicated file. View components **never** import from `src/data/mock*.ts` directly — they always go through the service layer.

```
View Component
      │
      ▼
src/services/api.ts        ← single import point for all consumers
      │
      ├── schemaService.ts      → src/data/mockSchema.ts
      ├── conceptService.ts     → src/data/mockConcepts.ts
      ├── analysisService.ts    → `/api/v1/analysis/dialect`
      ├── queryService.ts       → `/api/v1/analysis/queries/**`, `/api/v1/query/**` (HTTP only)
      ├── statsService.ts       → live schema/concept APIs + query catalog count
      ├── chatService.ts        → inline mock responses
      ├── inlineChatService.ts  → inline mock responses (3 pools)
      ├── chatReferencesService.ts → deterministic hash-based refs
      ├── relatedContentService.ts → deterministic cross-object refs (full hierarchy)
      └── featureService.ts     → returns defaultFeatureFlags
```

**Key rules:**
1. **Only `src/services/*.ts` files may import from `src/data/mock*.ts`**. View components, contexts, and hooks import exclusively from `src/services/api`.
2. **Each service file exports a single named constant** implementing its typed interface (e.g., `export const schemaService: SchemaService`).
3. **`api.ts` is a pure re-export barrel** — no logic, just `export { schemaService } from './schemaService'` etc.
4. **To swap mock→real for any service**, change the export in the individual service file. No view code changes required.

### Service Interfaces

Each domain has a typed service interface in `src/types/`:

| Interface | File | Methods |
|-----------|------|---------|
| `SchemaService` | `types/schema.ts` | `getTree()`, `getEntityById(id)`, `getEntityFacets(id)` |
| `ConceptService` | `types/context.ts` | `getConcepts(filter?)`, `getConceptById(id)`, `getCategories()`, `getTags()` |
| `QueryService` | `types/query.ts` | `executeQuery(sql)`, `getSavedQueries()`, `getSavedQueryById(id)` |
| `StatsService` | `types/stats.ts` | `getStats()` → `DashboardStats` |
| `ChatService` | `types/chat.ts` | `sendMessage(conversationId, message)` (AsyncGenerator) |
| `InlineChatService` | `types/inlineChat.ts` | `sendMessage(contextType, contextId, message)` (AsyncGenerator) |
| `ChatReferencesService` | `types/chatReferences.ts` | `getConversationsForContext(contextType, contextId)` |
| `RelatedContentService` | `types/relatedContent.ts` | `getRelatedContent(contextType, contextId)` |
| `FeatureFlagService` | `services/featureService.ts` | `getFlags()` → `Partial<FeatureFlags>` |

### Mock Data (backing store for mock services)

| File | Contents |
|------|----------|
| `src/data/mockSchema.ts` | Hierarchical schema tree (sales, inventory, analytics), facet data, `getEntityFacets()`, `findEntityById()` |
| `src/data/mockConcepts.ts` | 8 business concepts across 4 categories, `getConceptById()`, `getCategories()`, `getTags()`, `filterConcepts()` |
### Adding New Features / Mock Services

All new feature mocking **must** follow the centralized service pattern and account for the backend backlog documented in **`BACKEND-Backlog.md`**:

1. **Define the interface** in the appropriate `src/types/*.ts` file, shaped to match the expected backend contract from `BACKEND-Backlog.md`.
2. **Create a service file** in `src/services/` (e.g., `myFeatureService.ts`) that exports a mock implementation.
3. **Re-export from `api.ts`** — add `export { myFeatureService } from './myFeatureService'`.
4. **Consume only via `api.ts`** in view components and contexts.
5. **Never import mock data directly** in any file outside `src/services/`.

---

## Testing

### Infrastructure

| Component | Details |
|-----------|---------|
| Runner | Vitest v4.0.16 |
| Environment | jsdom |
| Config | `vitest.config.ts` |
| Setup | `src/test/setup.ts` |
| Component testing | `@testing-library/react` |
| Interaction testing | `@testing-library/user-event` |
| Assertions | `@testing-library/jest-dom` |

### Setup Polyfills (`src/test/setup.ts`)

Required for Mantine components in jsdom:
- `window.matchMedia` — needed by `useMantineColorScheme`
- `ResizeObserver` — needed by Mantine's `ScrollArea`
- `Element.prototype.scrollTo` — needed by MessageList auto-scroll

### Test Files (24 files, 250+ tests)

#### Component Tests (16 files)

| File | Tests | Covers |
|------|-------|--------|
| `ChatArea.test.tsx` | 8 | Title, empty state, send message, AI response, title update |
| `ChatAppShell.test.tsx` | 6 | Sidebar title, auto-create conversation, welcome message |
| `ChatSidebar.test.tsx` | 8 | Empty state, conversation items, delete button |
| `MessageInput.test.tsx` | 10 | Placeholder, send, Enter key, Shift+Enter, disabled, clear |
| `MessageList.test.tsx` | 9 | Empty state, messages, typing indicator |
| `MessageBubble.test.tsx` | 10 | User/assistant rendering, markdown, lists, code |
| `ConceptDetails.test.tsx` | 13 | Header, description, tags, SQL, related entities, metadata |
| `ContextSidebar.test.tsx` | 12 | Categories, concepts list, tags, filtering |
| `ContextLayout.test.tsx` | 5 | Sidebar title, empty state, concept list |
| `SchemaTree.test.tsx` | 8 | Empty state, nodes, auto-expand, selection, collapse |
| `EntityDetails.test.tsx` | 16 | Header, badges, tabs, descriptive content |
| `Facets.test.tsx` | 16 | DescriptiveFacet, StructuralFacet, RelationFacet |
| `DataModelLayout.test.tsx` | 5 | Sidebar title, empty state, schema tree |
| `LoginPage.test.tsx` | 9 | Brand, social buttons, email/password, onLogin |
| `NotFoundPage.test.tsx` | 5 | 404, title, message, navigation buttons |
| `AccessDeniedPage.test.tsx` | 5 | 403, title, message, navigation buttons |

#### Context Tests (4 files)

| File | Tests | Covers |
|------|-------|--------|
| `ChatContext.test.tsx` | 13 | Provider, create/delete/send, title generation, localStorage |
| `InlineChatContext.test.tsx` | 18 | Sessions, drawer, send, listeners, getSessionByContextId |
| `chatReducer.test.ts` | 15 | All ChatAction types |
| `inlineChatReducer.test.ts` | 11 | All InlineChatAction types |

#### Data & Feature Tests (4 files)

| File | Tests | Covers |
|------|-------|--------|
| `mockSchema.test.ts` | 12 | Tree structure, findEntityById, getEntityFacets |
| `mockConcepts.test.ts` | 15 | Concepts, getConceptById, categories, tags, filtering |
| `queryService.test.ts` | — | HTTP saved-query catalog + execution session wiring |
| `defaults.test.ts` | 9 | Flag types, defaults, interface compliance, related content flags |

### Running Tests

```bash
# Watch mode (local development)
npm test

# Single run (CI/build server)
npx vitest run

# With JUnit report (CI)
npx vitest run --reporter=junit --outputFile=test-results.xml
```

### Test Patterns

- **Mock at the barrel:** All tests mock `../../services/api` via `vi.mock`. This is the only mock boundary for data access — tests never mock `data/mock*.ts` directly in component tests.
- **Per-service mocks:** Each test's `vi.mock('../../services/api', () => ({...}))` provides only the services that test needs (e.g., `schemaService`, `conceptService`, `featureService`).
- **Async-aware assertions:** Components that load data via services use `waitFor()` from `@testing-library/react` for assertions on async content.
- **`vi.hoisted()` for mock data:** When `vi.mock` factory needs test data, define it via `vi.hoisted()` to ensure it's available before the hoisted mock runs.
- **Provider wrappers:** Tests wrap components in required providers (MantineProvider, ChatProvider, FeatureFlagProvider, InlineChatProvider, ChatReferencesProvider, RelatedContentProvider)
- **localStorage isolation:** `beforeEach(() => localStorage.clear())` for chat tests
- **renderHook:** Used for testing ChatContext and InlineChatContext hooks directly
- **userEvent:** Used for realistic user interaction simulation (typing, clicking)

---

## Future Backend Integration

> **See `BACKEND-Backlog.md`** for the complete gap analysis, existing backend inventory, and prioritized development backlog (items B-1 through B-27).

### Service Swap Pattern

Each service lives in its own file under `src/services/`. To swap from mock to real backend for any domain, change the export in that file. No view components or contexts need modification:

```typescript
// src/services/schemaService.ts — current (mock)
import { mockSchemaTree, getEntityFacets, findEntityById } from '../data/mockSchema';

const mockSchemaService: SchemaService = { /* ... mock impl ... */ };

export const schemaService: SchemaService = mockSchemaService;

// src/services/schemaService.ts — future (real)
import { httpClient } from './httpClient';

const realSchemaService: SchemaService = {
  async getTree() { return httpClient.get('/api/metadata/v1/explorer/tree'); },
  async getEntityById(id) { return httpClient.get(`/api/metadata/v1/entities/${id}`); },
  async getEntityFacets(id) { return httpClient.get(`/api/metadata/v1/entities/${id}/facets`); },
};

export const schemaService: SchemaService = realSchemaService;
```

The barrel (`api.ts`) re-exports remain unchanged — consumers always `import { schemaService } from '../services/api'`.

### Backend Endpoints Mapping

| Service | Current Mock File | Real Endpoint (from Backlog) | Backlog Items |
|---------|-------------------|------------------------------|---------------|
| Schema | `schemaService.ts` | `GET /api/metadata/v1/explorer/tree`, `/entities/{id}`, `/entities/{id}/facets` | B-1, B-2, B-3 |
| Concepts | `conceptService.ts` | `GET /api/v1/concepts`, `/concepts/{id}`, `/concepts/categories`, `/concepts/tags` | B-7 — B-11 |
| Analysis | `analysisService.ts`, `queryService.ts` | `GET /api/v1/analysis/dialect`, `GET/POST/PUT/DELETE /api/v1/analysis/queries`, `POST /api/v1/query`, `GET /api/v1/query/{executionId}`, `DELETE /api/v1/query/{executionId}` | B-4, B-12 — B-17 |
| Stats | `statsService.ts` | `GET /api/v1/stats` | B-18 |
| Chat | `chatService.ts` | `POST /api/nl2sql/chats/{id}/messages` (SSE) | B-5, B-6, B-14 |
| Inline Chat | `inlineChatService.ts` | `POST /api/v1/inline-chat/messages` (SSE) | B-20 |
| Chat References | `chatReferencesService.ts` | `GET /api/v1/chat-references?contextType=X&contextId=Y` | B-21 |
| Related Content | `relatedContentService.ts` | `GET /api/v1/related-content?contextType=X&contextId=Y` | B-23 |
| Feature Flags | `featureService.ts` | `GET /api/v1/features` | B-19 |
| Auth | Mock in `App.tsx` | OAuth2/OIDC provider | B-22 |

### Known Frontend TODOs for Backend

| Location | Item | Resolution |
|----------|------|------------|
| `src/App.tsx:47-48` | `APP_NAME` hardcoded | Load from `/.well-known/mill` `ApplicationDescriptor` |
| `src/context/ChatContext.tsx` | Conversation persistence via `localStorage` | Replace with chat service API (conversations endpoint) |
| `src/theme/ThemeContext.tsx` | Theme preferences via `localStorage` | Keep as-is (frontend concern) unless cross-device sync is needed |

### State Migration Path

The current architecture loads data in `useEffect` hooks with async service calls. When ready, convert to React Query or SWR for caching, deduplication, and background refresh:

```typescript
import { useQuery } from '@tanstack/react-query';

function DataModelLayout() {
  const { data: tree, isLoading } = useQuery({
    queryKey: ['schemaTree'],
    queryFn: () => schemaService.getTree(),
  });
}
```

The service interfaces remain the same — React Query wraps the existing service methods.

---

## Development Commands

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Type check
npx tsc --noEmit

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests (watch mode)
npm test

# Run tests (single run, CI-compatible)
npx vitest run

# Lint
npm run lint
```

---

## Key Design Decisions

1. **No backend required** — All data is mocked for standalone demo
2. **Centralized data layer** — All data access goes through `src/services/api.ts`; view components never import mock data directly. Each service is one file, each file implements a typed interface, and swapping mock→real is a single-file change. See `BACKEND-Backlog.md` for the full backend requirements.
3. **Feature flags everywhere** — Every view, section, and UI element can be toggled
4. **Mock authentication** — Simple context-based auth, ready for real OAuth2/OIDC swap
5. **LocalStorage persistence** — Chat conversations and theme preferences survive refresh
6. **URL sync for data views** — Deep linking support for all views
7. **Mantine UI v8** — Consistent component library with first-class dark mode
8. **React Router v7** — Modern routing with typed params and feature-flag gating
9. **Streaming simulation** — AsyncGenerator-based chat demonstrates real-world UX
10. **CollapsibleSidebar** — Shared sidebar component with `position: fixed` collapse for zero-width
11. **InlineChatButton** — Context-aware chat triggers placed in view-specific headers, not global nav
12. **Listener registry** — Views can react to AI responses (e.g., Analysis extracts SQL from chat)
13. **Popover confirmations** — Consistent pattern for destructive actions (delete, close all)
14. **Dynamic query management** — New Query / Delete Query mimics chat conversation management
15. **Export results** — CSV, Excel (TSV), JSON download from query results
16. **Comprehensive testing** — 250+ tests across 24 files covering components, contexts, reducers, and data
17. **Multiple color themes** — Independent light/dark theme selection with localStorage persistence
18. **Catch-all 404** — Unknown routes show NotFoundPage instead of silent redirect
19. **Related Content** — Cross-object relationships (model ↔ concept ↔ analysis) shown via `RelatedContentButton` with tree view for model hierarchy and flat list for non-model items. Same service/context/flag pattern as Chat References.

---

## Notes for Continuation

1. **Auth is mock** — `AuthContext` in `App.tsx` uses `useState`; replace with real auth provider
2. **Feature flags default to `true`** — All UI is visible in development; toggle in `defaults.ts`
3. **Chat state is scoped** — `ChatProvider` wraps ChatView only; `InlineChatProvider` is app-wide
4. **Data Model uses URL params** — Entity ID format is `schema.table.column`
5. **Data layer is centralized** — All data access goes through `src/services/api.ts`. Never import `src/data/mock*.ts` from views, contexts, or hooks. See [Data Layer](#data-layer) section.
6. **Adding mock features** — Any new feature mock **must** follow the same pattern: interface in `types/`, service file in `services/`, re-export from `api.ts`. Shape the interface to match the expected backend contract documented in `BACKEND-Backlog.md`.
7. **`mockApi.ts` is legacy** — The original monolithic mock file is superseded by the per-file services. Do not add new code there.
8. **Icons are from Heroicons** — Import from `react-icons/hi2`
9. **All heights are 100%** — Root is `100vh`
10. **Inline chats are ephemeral** — Not persisted; stored in React state only
11. **Two response pool systems** — Main chat (5 general responses), inline chat (3 pools: model, context, query)
12. **Confirmation pattern** — All destructive actions use Mantine `Popover` with Cancel/Confirm
13. **Admin tab is right-aligned** — Separated from main nav via `justifyContent: 'space-between'` in `Tabs.List`
14. **Login providers use inline SVG** — Custom icons for GitHub, Google, Microsoft, AWS, Azure
15. **Test setup requires polyfills** — `matchMedia`, `ResizeObserver`, `scrollTo` for jsdom + Mantine
16. **`vitest run` for CI** — Watch mode (`vitest`) hangs on build servers; use `vitest run` for single execution
17. **Related Content assumes full hierarchy** — The backend always provides the full model path (schema + table + column) in related content responses. The `RelatedModelTree` component is defensive and will create dimmed placeholders if ancestors are missing, but the contract expects complete chains.
18. **Three indicator systems** — `InlineChatButton` (cyan/teal, inline sessions), Chat References (violet, related general chats), Related Content (indigo, cross-object relationships). All three share the same service→context→hook→button pattern.

---

*Last updated: February 12, 2026*
