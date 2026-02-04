# DataChat - Architecture & Implementation Guide

## Overview

DataChat is a modern AI chat interface with integrated data model exploration and business context browsing. Built with React 19, TypeScript, Vite, and Mantine UI v8.

**Live URL:** http://localhost:5173/

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [Application Architecture](#application-architecture)
4. [Views & Features](#views--features)
5. [Design System](#design-system)
6. [State Management](#state-management)
7. [Routing](#routing)
8. [Mock Data Layer](#mock-data-layer)
9. [Component Documentation](#component-documentation)
10. [Future Backend Integration](#future-backend-integration)
11. [Development Commands](#development-commands)

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
| @tanstack/react-table | ^8.21.3 | Table component (available) |
| axios | ^1.10.0 | HTTP client (for future API) |
| echarts-for-react | ^3.0.2 | Charts (available) |

---

## Project Structure

```
src/
├── components/
│   ├── layout/                    # App shell & navigation
│   │   ├── AppHeader.tsx          # Top navigation bar with tabs
│   │   ├── AppShell.tsx           # Chat view container with sidebar
│   │   └── Sidebar.tsx            # Chat conversation list sidebar
│   │
│   ├── chat/                      # Chat feature components
│   │   ├── ChatArea.tsx           # Main chat container
│   │   ├── MessageList.tsx        # Scrollable message container
│   │   ├── MessageBubble.tsx      # Individual message with markdown
│   │   ├── MessageInput.tsx       # Text input with send button
│   │   ├── TypingIndicator.tsx    # AI typing animation
│   │   └── TypingIndicator.css    # Keyframe animation
│   │
│   ├── common/                    # Shared components
│   │   └── CodeBlock.tsx          # Syntax highlighted code
│   │
│   ├── data-model/                # Data Model Explorer view
│   │   ├── DataModelLayout.tsx    # Main layout with sidebar + content
│   │   ├── SchemaTree.tsx         # Expandable schema/table/column tree
│   │   ├── EntityDetails.tsx      # Selected entity display with tabs
│   │   └── facets/                # Facet viewer components
│   │       ├── DescriptiveFacet.tsx   # Name, description, tags
│   │       ├── StructuralFacet.tsx    # Type, constraints, defaults
│   │       └── RelationFacet.tsx      # FK relationships
│   │
│   └── context/                   # Context Explorer view
│       ├── ContextLayout.tsx      # Main layout with sidebar + content
│       ├── ContextSidebar.tsx     # Categories, tags, concept list
│       └── ConceptDetails.tsx     # Selected concept display
│
├── context/                       # React Context providers
│   └── ChatContext.tsx            # Chat state management
│
├── data/                          # Mock data (localStorage simulation)
│   ├── mockSchema.ts              # Schema tree + facets data
│   └── mockConcepts.ts            # Business concepts data
│
├── services/                      # API layer
│   ├── api.ts                     # Service interface exports
│   └── mockApi.ts                 # Mock chat responses
│
├── types/                         # TypeScript type definitions
│   ├── chat.ts                    # Chat, Message, Conversation types
│   ├── schema.ts                  # Schema, Entity, Facet types
│   └── context.ts                 # Concept types
│
├── theme/                         # Mantine theme configuration
│   └── theme.ts                   # Colors, fonts, component defaults
│
├── App.tsx                        # Root component with routing
├── main.tsx                       # Entry point with providers
└── index.css                      # Global styles
```

---

## Application Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────┐
│                         App.tsx                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    MantineProvider                       ││
│  │  ┌─────────────────────────────────────────────────────┐││
│  │  │                   BrowserRouter                      │││
│  │  │  ┌─────────────────────────────────────────────────┐│││
│  │  │  │                  AppHeader                       ││││
│  │  │  │         [Chat] [Data Model] [Context]           ││││
│  │  │  └─────────────────────────────────────────────────┘│││
│  │  │  ┌─────────────────────────────────────────────────┐│││
│  │  │  │                   Routes                         ││││
│  │  │  │  /chat/*        → ChatView (with ChatProvider)  ││││
│  │  │  │  /data-model/*  → DataModelLayout               ││││
│  │  │  │  /context/*     → ContextLayout                 ││││
│  │  │  └─────────────────────────────────────────────────┘│││
│  │  └─────────────────────────────────────────────────────┘││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Component Hierarchy

```
App
├── AppHeader (navigation tabs, theme toggle)
└── Routes
    ├── /chat/* → ChatView
    │   └── ChatProvider (context)
    │       └── AppShell
    │           ├── Sidebar (conversation list)
    │           └── ChatArea
    │               ├── MessageList
    │               │   └── MessageBubble (multiple)
    │               │       └── ReactMarkdown + CodeBlock
    │               └── MessageInput
    │
    ├── /data-model/* → DataModelLayout
    │   ├── SchemaTree (sidebar)
    │   │   └── TreeNode (recursive)
    │   └── EntityDetails (main content)
    │       └── Tabs
    │           ├── DescriptiveFacet
    │           ├── StructuralFacet
    │           └── RelationFacet
    │
    └── /context/* → ContextLayout
        ├── ContextSidebar
        │   ├── Categories (NavLink list)
        │   ├── Tags (NavLink list)
        │   └── ConceptList (NavLink list)
        └── ConceptDetails (main content)
```

---

## Views & Features

### 1. Chat View (`/chat`)

**Purpose:** AI chat interface with conversation management

**Features:**
- Conversation sidebar with history
- New chat creation
- Delete conversations
- Message bubbles (user right-aligned, AI left-aligned)
- Markdown rendering with syntax highlighting
- Streaming AI responses (simulated)
- Typing indicator animation
- Auto-scroll to latest message
- Settings modal (theme toggle, clear history)
- LocalStorage persistence

**Key Components:**
- `ChatContext.tsx` - State management with useReducer
- `MessageBubble.tsx` - Renders markdown with code blocks
- `MessageInput.tsx` - Auto-resize textarea, Enter to send

**State Shape:**
```typescript
interface ChatState {
  conversations: Conversation[];
  activeConversationId: string | null;
  isLoading: boolean;
}

interface Conversation {
  id: string;
  title: string;           // Auto-generated from first message
  createdAt: number;
  updatedAt: number;
  messages: Message[];
}

interface Message {
  id: string;
  conversationId: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}
```

### 2. Data Model View (`/data-model`)

**Purpose:** Browse database schema with metadata facets

**URL Pattern:** `/data-model/:schema?/:table?/:attribute?`

**Features:**
- Expandable tree navigation (Schema → Table → Column)
- Click to select and view details
- Entity header with type badge and constraints
- Tabbed facet display:
  - **Descriptive:** Display name, description, business meaning, tags, synonyms
  - **Structural:** Physical type, PK/FK/Unique/Nullable, defaults
  - **Relations:** FK relationships with cardinality
- URL synchronization for deep linking

**Key Components:**
- `SchemaTree.tsx` - Recursive tree with NavLink
- `EntityDetails.tsx` - Header + tabbed facets
- `facets/*.tsx` - Individual facet renderers

**Data Types:**
```typescript
type EntityType = 'SCHEMA' | 'TABLE' | 'ATTRIBUTE';

interface SchemaEntity {
  id: string;              // e.g., "sales.customers.customer_id"
  type: EntityType;
  name: string;
  children?: SchemaEntity[];
}

interface EntityFacets {
  descriptive?: DescriptiveFacet;
  structural?: StructuralFacet;
  relations?: RelationFacet[];
}
```

### 3. Context View (`/context`)

**Purpose:** Browse business concepts/definitions

**URL Pattern:** `/context/:conceptId?`

**Features:**
- Category filter with counts
- Tag filter with counts
- Filtered concept list
- Concept details:
  - Name, category, source badge
  - Description
  - Tags
  - SQL definition (syntax highlighted)
  - Related entities
- Click category/tag toggles filter (click again to clear)

**Key Components:**
- `ContextSidebar.tsx` - Categories, tags, concept list
- `ConceptDetails.tsx` - Full concept display

**Data Types:**
```typescript
interface Concept {
  id: string;
  name: string;
  category: string;        // "Analytics", "Customer", "Operations", "Product"
  tags: string[];          // ["revenue", "customer", "metric"]
  description: string;
  sql?: string;            // Optional SQL definition
  relatedEntities?: string[];  // Entity IDs from schema
  source?: 'MANUAL' | 'INFERRED' | 'IMPORTED';
  createdAt?: number;
}
```

---

## Design System

### Color Palette

**Primary Colors (Teal/Cyan):**
```typescript
// Light mode primary: Teal
teal: ['#f0fdfa', '#ccfbf1', '#99f6e4', '#5eead4', '#2dd4bf', 
       '#14b8a6', '#0d9488', '#0f766e', '#115e59', '#134e4a']

// Dark mode accent: Cyan
cyan: ['#ecfeff', '#cffafe', '#a5f3fc', '#67e8f9', '#22d3ee',
       '#06b6d4', '#0891b2', '#0e7490', '#155e75', '#164e63']

// Neutral: Slate
slate: ['#f8fafc', '#f1f5f9', '#e2e8f0', '#cbd5e1', '#94a3b8',
        '#64748b', '#475569', '#334155', '#1e293b', '#0f172a']
```

**Usage:**
| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| Primary buttons | `teal.6` (#0d9488) | `cyan.6` (#0891b2) |
| Active nav | `teal.1` bg | `cyan.9` bg |
| User message | `teal.6` bg | `cyan.7` bg |
| AI message | `gray.1` bg | `slate.7` bg |
| Sidebar | `slate.1` bg | `slate.9` bg |
| Main content | `white` bg | `slate.8` bg |

### Component Styling

**Borders:**
```css
/* Light */ border-color: var(--mantine-color-gray-3)
/* Dark */  border-color: var(--mantine-color-slate-7)
```

**Shadows:** Minimal, using Mantine defaults (`xs`, `sm`)

**Border Radius:**
- Default: `md` (8px)
- Cards: `lg` (12px)
- Buttons: `md` (8px)
- Message bubbles: 16px with asymmetric corners

**Typography:**
- Font: System font stack (-apple-system, BlinkMacSystemFont, ...)
- Sizes: Mantine scale (xs, sm, md, lg, xl)

### Icon Usage

Using `react-icons/hi2` (Heroicons v2 Outline):
- Chat: `HiOutlineChatBubbleLeftRight`
- Data Model: `HiOutlineCircleStack`
- Context: `HiOutlineLightBulb`
- Schema: `HiOutlineCircleStack`
- Table: `HiOutlineTableCells`
- Column: `HiOutlineViewColumns`
- Settings: `HiOutlineCog6Tooth`
- Add: `HiOutlinePlus`
- Delete: `HiOutlineTrash`
- Send: `HiPaperAirplane`

---

## State Management

### Chat State (React Context + useReducer)

**Location:** `src/context/ChatContext.tsx`

**Pattern:** Reducer-based with actions
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
- Loads on mount
- Saves on every conversation change

**Streaming Simulation:**
- Creates empty assistant message immediately
- Updates message content incrementally via `UPDATE_MESSAGE`
- Uses AsyncGenerator in `mockApi.ts`

### Data Model State (Component-level)

**Location:** `src/components/data-model/DataModelLayout.tsx`

**Pattern:** useState with URL sync
```typescript
const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);

// Sync from URL on mount/change
useEffect(() => {
  const entityId = buildIdFromParams(params);
  const entity = findEntityById(entityId);
  setSelectedEntity(entity);
}, [params]);

// Update URL on selection
const handleSelect = (entity: SchemaEntity) => {
  setSelectedEntity(entity);
  navigate(`/data-model/${entity.id.replace(/\./g, '/')}`);
};
```

### Context State (Component-level)

**Location:** `src/components/context/ContextLayout.tsx`

**Pattern:** useState with filter state
```typescript
const [selectedConcept, setSelectedConcept] = useState<Concept | null>(null);
const [filter, setFilter] = useState<ConceptFilter>({ type: null, value: null });
```

---

## Routing

### Setup

**Location:** `src/main.tsx`
```typescript
<BrowserRouter>
  <App />
</BrowserRouter>
```

### Routes

**Location:** `src/App.tsx`
```typescript
<Routes>
  <Route path="/chat/*" element={<ChatView />} />
  <Route path="/data-model/:schema?/:table?/:attribute?" element={<DataModelLayout />} />
  <Route path="/context/:conceptId?" element={<ContextLayout />} />
  <Route index element={<Navigate to="/chat" replace />} />
  <Route path="*" element={<Navigate to="/chat" replace />} />
</Routes>
```

### URL Patterns

| View | Pattern | Example |
|------|---------|---------|
| Chat | `/chat` | `/chat` |
| Data Model | `/data-model/:schema?/:table?/:attribute?` | `/data-model/sales/customers/customer_id` |
| Context | `/context/:conceptId?` | `/context/customer-lifetime-value` |

### Navigation

**AppHeader** uses `useLocation` to determine active tab and `useNavigate` for programmatic navigation.

---

## Mock Data Layer

### Schema Data

**Location:** `src/data/mockSchema.ts`

**Contents:**
- `mockSchemaTree` - Hierarchical schema structure
- `mockFacets` - Facet data keyed by entity ID
- `getEntityFacets(id)` - Lookup function
- `findEntityById(id, tree)` - Recursive search

**Sample Schema:**
```
sales/
├── customers/
│   ├── customer_id (PK)
│   ├── name
│   ├── email (Unique)
│   ├── created_at
│   └── segment
├── orders/
│   ├── order_id (PK)
│   ├── customer_id (FK)
│   ├── order_date
│   ├── total_amount
│   └── status
└── order_items/
    ├── item_id (PK)
    ├── order_id (FK)
    ├── product_id (FK)
    ├── quantity
    └── unit_price

inventory/
├── products/
└── suppliers/

analytics/
├── daily_sales/
└── customer_metrics/
```

### Concept Data

**Location:** `src/data/mockConcepts.ts`

**Contents:**
- `mockConcepts` - Array of business concepts
- `getConceptById(id)` - Lookup function
- `getCategories()` - Unique categories with counts
- `getTags()` - Unique tags with counts
- `filterConcepts(type, value)` - Filter by category or tag

**Categories:** Analytics, Customer, Operations, Product

**Sample Tags:** revenue, customer, metric, orders, operations, classification, etc.

### Chat Mock API

**Location:** `src/services/mockApi.ts`

**Features:**
- 5 sample responses with varied content (code, lists, tables)
- Streaming simulation via AsyncGenerator
- Random delay per word (30-80ms)
- Initial delay (500ms) to simulate network

**Interface:**
```typescript
interface ChatService {
  sendMessage(conversationId: string, message: string): AsyncGenerator<string, void, unknown>;
}
```

---

## Component Documentation

### AppHeader

**File:** `src/components/layout/AppHeader.tsx`

**Props:** None

**Features:**
- Logo with gradient background
- Tab navigation using Mantine Tabs (pills variant)
- Theme toggle button
- Determines active tab from `useLocation`

### SchemaTree

**File:** `src/components/data-model/SchemaTree.tsx`

**Props:**
```typescript
interface SchemaTreeProps {
  tree: SchemaEntity[];
  selectedId: string | null;
  onSelect: (entity: SchemaEntity) => void;
}
```

**Features:**
- Recursive `TreeNode` component
- Auto-expand first level
- Chevron icons for expandable nodes
- Active state highlighting

### EntityDetails

**File:** `src/components/data-model/EntityDetails.tsx`

**Props:**
```typescript
interface EntityDetailsProps {
  entity: SchemaEntity;
  facets: EntityFacets;
}
```

**Features:**
- Header with icon, name, type badge
- Constraint badges (PK, FK, Unique, Not Null, type)
- Conditional tabs based on available facets

### MessageBubble

**File:** `src/components/chat/MessageBubble.tsx`

**Props:**
```typescript
interface MessageBubbleProps {
  message: Message;
}
```

**Features:**
- User messages: plain text, right-aligned
- AI messages: ReactMarkdown with custom renderers
- Code blocks via CodeBlock component
- Tables with proper styling
- Dark/light mode colors

---

## Future Backend Integration

### Chat API

Replace `src/services/api.ts` export:

```typescript
// Current (mock)
export const chatService: ChatService = mockChatService;

// Future (real)
export const chatService: ChatService = {
  async *sendMessage(conversationId: string, message: string) {
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conversationId, message }),
    });
    
    const reader = response.body?.getReader();
    const decoder = new TextDecoder();
    
    while (reader) {
      const { done, value } = await reader.read();
      if (done) break;
      yield decoder.decode(value);
    }
  },
};
```

### Schema API

Create `src/services/schemaApi.ts`:

```typescript
import axios from 'axios';
import type { SchemaEntity, EntityFacets } from '../types/schema';

const API_BASE = '/api/schema';

export const schemaApi = {
  async getTree(): Promise<SchemaEntity[]> {
    const { data } = await axios.get(`${API_BASE}/tree`);
    return data;
  },
  
  async getEntityFacets(entityId: string): Promise<EntityFacets> {
    const { data } = await axios.get(`${API_BASE}/entities/${entityId}/facets`);
    return data;
  },
};
```

### Context API

Create `src/services/contextApi.ts`:

```typescript
import axios from 'axios';
import type { Concept } from '../types/context';

const API_BASE = '/api/context';

export const contextApi = {
  async getConcepts(): Promise<Concept[]> {
    const { data } = await axios.get(`${API_BASE}/concepts`);
    return data;
  },
  
  async getConceptById(id: string): Promise<Concept> {
    const { data } = await axios.get(`${API_BASE}/concepts/${id}`);
    return data;
  },
};
```

### State Updates for API

Convert from direct mock imports to React Query or SWR:

```typescript
// Example with React Query (add @tanstack/react-query)
import { useQuery } from '@tanstack/react-query';

function DataModelLayout() {
  const { data: tree, isLoading } = useQuery({
    queryKey: ['schemaTree'],
    queryFn: schemaApi.getTree,
  });
  
  // ...
}
```

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

# Run tests
npm run test

# Lint
npm run lint
```

---

## Visual Design System

### Design Principles

#### 1. Clarity Over Decoration
- Minimal visual noise; content is the focus
- No unnecessary gradients, shadows, or decorative elements
- Clean lines and generous whitespace
- Icons are functional, not decorative

#### 2. Consistent Visual Language
- Same patterns repeated across all views
- Sidebar + main content layout throughout
- Uniform header treatment for all detail views
- Consistent badge and tag styling

#### 3. Progressive Disclosure
- Information revealed as needed (expandable trees, tabs)
- Empty states guide users to action
- Tooltips for secondary information
- Collapsible sections for dense data

#### 4. Accessible by Default
- High contrast text (WCAG AA compliant)
- Focus states on interactive elements
- Color not the only indicator (icons + color for status)
- Readable font sizes (minimum 12px)

#### 5. Responsive & Adaptive
- Sidebar collapses on mobile
- Flexible layouts that reflow
- Touch-friendly tap targets (minimum 44px)
- Dark mode as first-class citizen

---

### Color System

#### Philosophy
The color palette is built around **Teal** (trust, calm, professional) with **Cyan** accents for dark mode vibrancy. **Slate** provides neutral grays that work in both modes.

#### Primary Palette: Teal
Used for primary actions, active states, and brand identity.

```
Index:  0        1        2        3        4        5        6        7        8        9
Light ─────────────────────────────────────────────────────────────────────────────────► Dark
       #f0fdfa  #ccfbf1  #99f6e4  #5eead4  #2dd4bf  #14b8a6  #0d9488  #0f766e  #115e59  #134e4a
       ░░░░░░░  ░░░░░░░  ░░░░░░░  ████████ ████████ ████████ ████████ ████████ ████████ ████████

Usage:
- teal.0: Light mode backgrounds (subtle highlights)
- teal.1: Light mode active/hover states
- teal.5-6: Primary buttons, links (light mode)
- teal.7-9: Text on light backgrounds
```

#### Accent Palette: Cyan
Used in dark mode for better visibility and vibrancy.

```
Index:  0        1        2        3        4        5        6        7        8        9
       #ecfeff  #cffafe  #a5f3fc  #67e8f9  #22d3ee  #06b6d4  #0891b2  #0e7490  #155e75  #164e63

Usage:
- cyan.4: Text accents in dark mode
- cyan.6-7: Primary buttons (dark mode)
- cyan.9: Background tints in dark mode
```

#### Neutral Palette: Slate
Full-spectrum gray with slight blue undertone for modern feel.

```
Index:  0        1        2        3        4        5        6        7        8        9
       #f8fafc  #f1f5f9  #e2e8f0  #cbd5e1  #94a3b8  #64748b  #475569  #334155  #1e293b  #0f172a

Usage:
Light Mode:
- slate.0-1: Sidebar backgrounds
- slate.2-3: Borders, dividers
- slate.5-6: Secondary text
- slate.7-8: Primary text

Dark Mode:
- slate.7: Borders, card backgrounds
- slate.8: Main content background
- slate.9: Sidebar, header backgrounds
- slate.1-2: Primary text
```

#### Semantic Colors

| Purpose | Color | Light Mode | Dark Mode |
|---------|-------|------------|-----------|
| Success | Green | `green.6` | `green.5` |
| Error/Danger | Red | `red.6` | `red.5` |
| Warning | Orange | `orange.6` | `orange.5` |
| Info | Blue | `blue.6` | `blue.5` |
| Foreign Key | Orange | `orange.6` filled | `orange.6` filled |
| Primary Key | Teal | `teal.6` filled | `cyan.6` filled |
| Unique | Teal | `teal.6` light | `cyan.6` light |
| Nullable | Gray | `gray.6` light | `slate.5` light |

---

### Typography

#### Font Stack
```css
font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, 
             "Helvetica Neue", Arial, sans-serif;
```
System fonts for optimal performance and native feel.

#### Type Scale (Mantine)

| Name | Size | Line Height | Usage |
|------|------|-------------|-------|
| `xs` | 12px | 1.4 | Captions, timestamps, badges |
| `sm` | 14px | 1.45 | Body text, descriptions |
| `md` | 16px | 1.5 | Default, input text |
| `lg` | 18px | 1.5 | Section headers |
| `xl` | 20px | 1.4 | Page titles |

#### Font Weights

| Weight | Value | Usage |
|--------|-------|-------|
| Regular | 400 | Body text |
| Medium | 500 | Active items, emphasis |
| Semi-bold | 600 | Headers, labels |
| Bold | 700 | Strong emphasis (rare) |

#### Monospace
```css
font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, 
             Consolas, "Liberation Mono", monospace;
```
Used for: Entity IDs, SQL code, technical values, code blocks.

---

### Spacing System

#### Base Unit: 4px
All spacing is multiples of 4px for consistency.

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4px | Tight gaps (badge padding) |
| `sm` | 8px | Related items (icon + text) |
| `md` | 16px | Default padding, section gaps |
| `lg` | 24px | Major section separation |
| `xl` | 32px | Page-level padding |

#### Component Spacing

```
┌─────────────────────────────────────────────────┐
│ Header (h=56px)                          p="md" │
├─────────────────────────────────────────────────┤
│ ┌─────────┐ ┌─────────────────────────────────┐ │
│ │ Sidebar │ │ Main Content                    │ │
│ │ w=280px │ │                                 │ │
│ │ p="xs"  │ │ p="md"                         │ │
│ │         │ │                                 │ │
│ │         │ │ ┌─────────────────────────────┐ │ │
│ │         │ │ │ Card              p="sm/md" │ │ │
│ │         │ │ └─────────────────────────────┘ │ │
│ └─────────┘ └─────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

#### Gap Patterns

| Context | Gap |
|---------|-----|
| Inline badges | `xs` (4px) |
| Form fields | `sm` (8px) |
| List items | `sm` (8px) |
| Card sections | `md` (16px) |
| Stack sections | `lg` (24px) |

---

### Border & Shadow

#### Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4px | Small badges, inline elements |
| `sm` | 6px | Buttons, inputs |
| `md` | 8px | Cards, modals (default) |
| `lg` | 12px | Large cards, panels |
| `xl` | 16px | Hero elements |
| `50%` | - | Circular icons, avatars |

#### Message Bubbles (Custom)
```css
/* User message (right-aligned) */
border-radius: 16px 16px 4px 16px;

/* AI message (left-aligned) */
border-radius: 16px 16px 16px 4px;
```
Asymmetric corners indicate message direction.

#### Borders

| Context | Light Mode | Dark Mode |
|---------|------------|-----------|
| Sidebar | `1px solid gray.3` | `1px solid slate.7` |
| Cards | `1px solid gray.3` | `1px solid slate.7` |
| Inputs | `1px solid gray.3` | `1px solid slate.6` |
| Dividers | `gray.3` | `slate.7` |

#### Shadows
Minimal shadow usage for flat, modern aesthetic.

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | `0 1px 2px rgba(0,0,0,0.05)` | Subtle lift (cards) |
| `sm` | `0 1px 3px rgba(0,0,0,0.1)` | Message bubbles |
| `md` | `0 4px 6px rgba(0,0,0,0.1)` | Modals, dropdowns |

---

### Layout Patterns

#### Master Layout
```
┌────────────────────────────────────────────────────────────┐
│ AppHeader (fixed, h=56px)                                  │
│ [Logo]     [Chat] [Data Model] [Context]        [Theme 🌙] │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  View Content (flex: 1, overflow: hidden)                  │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

#### Sidebar + Content Pattern
Used by all three views:

```
┌──────────────────────────────────────────────────────────┐
│ ┌──────────────┐ ┌─────────────────────────────────────┐ │
│ │   Sidebar    │ │         Main Content               │ │
│ │   w=280px    │ │         flex: 1                    │ │
│ │              │ │                                     │ │
│ │  - Header    │ │  - Header (gradient bg)            │ │
│ │  - Scroll    │ │  - Scrollable content              │ │
│ │    Area      │ │  - Optional footer                 │ │
│ │  - Footer    │ │                                     │ │
│ └──────────────┘ └─────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

#### Empty States
Centered content with:
1. Large icon in circular background (80x80px)
2. Title text (xl, semi-bold)
3. Description text (sm, dimmed, max-width 400px)

```
            ┌─────────────┐
            │    Icon     │
            │   (80px)    │
            └─────────────┘
         
           Title Goes Here
        
     Supporting description text that
     explains what to do next.
```

---

### Interactive States

#### Buttons

| State | Change |
|-------|--------|
| Default | Base color |
| Hover | Slightly darker (`color.7` → `color.8`) |
| Active/Pressed | Even darker, slight scale(0.98) |
| Disabled | 50% opacity, no pointer events |
| Loading | Spinner replaces content |

#### Navigation Items (NavLink)

| State | Light Mode | Dark Mode |
|-------|------------|-----------|
| Default | Transparent | Transparent |
| Hover | `slate.2` | `slate.8` |
| Active | `teal.1` bg, `teal.7` text | `cyan.9` bg, `cyan.4` text |

#### Form Inputs

| State | Border Color |
|-------|--------------|
| Default | `gray.3` / `slate.6` |
| Focus | `teal.6` / `cyan.6` |
| Error | `red.6` |
| Disabled | `gray.2` / `slate.7` + reduced opacity |

---

### Animation & Motion

#### Principles
- **Purposeful**: Animation serves UX, not decoration
- **Quick**: 150-300ms duration
- **Easing**: `ease` or `ease-out` for natural feel

#### Transitions

| Element | Property | Duration | Easing |
|---------|----------|----------|--------|
| Background color | `background-color` | 150ms | ease |
| Border color | `border-color` | 150ms | ease |
| Transform | `transform` | 200ms | ease-out |
| Opacity | `opacity` | 200ms | ease |
| Sidebar slide | `transform` | 300ms | ease |

#### Keyframe Animations

**Typing Indicator Bounce:**
```css
@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}
/* 3 dots with staggered delay: 0s, 0.16s, 0.32s */
```

**Message Fade-In:**
```css
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* Duration: 300ms */
```

#### Scroll Behavior
```css
scroll-behavior: smooth;
```
Used for auto-scroll to latest message.

---

### Dark Mode Implementation

#### Strategy
Mantine's `useMantineColorScheme` hook provides:
- `colorScheme`: 'light' | 'dark' | 'auto'
- `toggleColorScheme`: Function to switch

#### Color Mapping

| Element | Light | Dark |
|---------|-------|------|
| Page background | `white` | `slate.8` |
| Sidebar background | `slate.0/1` | `slate.9` |
| Card background | `white` | `slate.8` |
| Primary text | `slate.8` | `slate.1` |
| Secondary text | `slate.5/6` | `slate.4/5` |
| Borders | `gray.3` | `slate.7` |
| Primary button | `teal.6` | `cyan.6` |
| User message | `teal.6` bg | `cyan.7` bg |
| AI message | `gray.1` bg | `slate.7` bg |

#### CSS Variables
Mantine provides CSS variables that update automatically:
```css
var(--mantine-color-teal-6)
var(--mantine-color-slate-8)
var(--mantine-color-body)  /* auto light/dark */
```

#### Gradient Backgrounds
Headers use subtle gradients:
```typescript
// Light
background: linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)

// Dark
background: linear-gradient(135deg, var(--mantine-color-slate-9) 0%, var(--mantine-color-slate-8) 100%)
```

---

### Iconography

#### Icon Library
**Heroicons v2 (Outline)** via `react-icons/hi2`

#### Icon Sizes

| Context | Size | Example |
|---------|------|---------|
| Inline with text | 14-16px | NavLink icons |
| Buttons | 16-18px | Action icons |
| Section headers | 20px | Entity type icons |
| Empty states | 32-36px | Placeholder icons |

#### Common Icons

| Icon | Component | Usage |
|------|-----------|-------|
| Chat | `HiOutlineChatBubbleLeftRight` | Chat nav, messages |
| Database | `HiOutlineCircleStack` | Schema, Data Model nav |
| Lightbulb | `HiOutlineLightBulb` | Context nav, concepts |
| Table | `HiOutlineTableCells` | Table entities |
| Columns | `HiOutlineViewColumns` | Column/attribute entities |
| Plus | `HiOutlinePlus` | Add/create actions |
| Trash | `HiOutlineTrash` | Delete actions |
| Settings | `HiOutlineCog6Tooth` | Settings |
| Sun/Moon | `HiOutlineSun/Moon` | Theme toggle |
| Chevron | `HiChevronRight/Down` | Expandable items |
| Arrow | `HiArrowLongRight` | Relationships |

#### Icon + Text Alignment
```tsx
<Group gap="xs">
  <Icon size={16} />
  <Text>Label</Text>
</Group>
```
Icons vertically centered with text.

---

### Responsive Design

#### Breakpoints

| Name | Width | Behavior |
|------|-------|----------|
| Mobile | < 768px | Sidebar hidden, toggle to show |
| Desktop | ≥ 768px | Sidebar always visible |

#### Mobile Adaptations

1. **Sidebar**
   - Hidden by default
   - Toggle button in header
   - Slides in from left
   - Overlay behind (click to close)

2. **Content**
   - Full width
   - Reduced padding
   - Stacked layouts where needed

#### Implementation
```typescript
const isMobile = useMediaQuery('(max-width: 768px)');

// Sidebar position
style={{
  position: isMobile ? 'fixed' : 'relative',
  transform: isMobile && !open ? 'translateX(-100%)' : 'none',
}}
```

---

### Visual Hierarchy

#### Importance Levels

| Level | Treatment | Example |
|-------|-----------|---------|
| 1 (Primary) | Large, bold, full color | Page titles, entity names |
| 2 (Secondary) | Medium, semi-bold | Section headers, labels |
| 3 (Body) | Normal weight | Descriptions, content |
| 4 (Supporting) | Small, dimmed | Timestamps, hints |
| 5 (Metadata) | Extra small, monospace | IDs, technical info |

#### Content Hierarchy Example (Entity Details)

```
┌─────────────────────────────────────────────────┐
│ [Icon] Customer ID          ← Level 1 (lg, 600) │
│        sales.customers.customer_id  ← Level 5   │
│        [PK] [INTEGER] [Not Null]   ← Badges     │
├─────────────────────────────────────────────────┤
│ DISPLAY NAME                ← Level 4 (xs, dim) │
│ Customer ID                 ← Level 2 (sm, 500) │
│                                                 │
│ DESCRIPTION                 ← Level 4           │
│ Unique identifier for...    ← Level 3 (sm)     │
└─────────────────────────────────────────────────┘
```

---

### Badge System

#### Variants

| Variant | Use Case |
|---------|----------|
| `filled` | Strong emphasis (PK, FK, category) |
| `light` | Medium emphasis (tags, status) |
| `outline` | Low emphasis (type info, counts) |

#### Sizing

| Size | Height | Usage |
|------|--------|-------|
| `xs` | 18px | Inline counts |
| `sm` | 22px | Tags, constraints |
| `md` | 26px | Default |

#### Color Coding

| Meaning | Color | Variant |
|---------|-------|---------|
| Primary Key | teal/cyan | filled |
| Foreign Key | orange | filled |
| Unique | teal/cyan | light |
| Not Null | red | light |
| Nullable | gray | light |
| Data Type | gray | outline |
| Category | teal/cyan | light |
| Tag | teal/cyan | light |
| Source: Manual | teal | outline |
| Source: Inferred | violet | outline |

---

## Key Design Decisions

1. **No backend required** - All data is mocked for standalone demo
2. **LocalStorage for chat persistence** - Survives page refresh
3. **URL sync for data/context views** - Deep linking support
4. **Mantine UI** - Consistent component library with dark mode
5. **React Router v7** - Modern routing with typed params
6. **Streaming simulation** - Demonstrates real-world chat UX
7. **Faceted entity details** - Flexible metadata display
8. **Filter-based context browsing** - Category/tag discovery

---

## Notes for Continuation

1. **Theme is in `src/theme/theme.ts`** - Teal/Cyan/Slate palette
2. **Chat state is in Context** - ChatProvider wraps chat view only
3. **Data Model uses URL params** - Entity ID is `schema.table.column`
4. **Mock data is static** - Edit `src/data/*.ts` to add more
5. **Icons are from Heroicons** - Import from `react-icons/hi2`
6. **All heights are 100%** - Except root which is 100vh

---

*Last updated: February 5, 2026*
