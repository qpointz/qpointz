# Mill Grinder UI Visual Refresh

## Overview

This document describes the UX refresh implementation for the Mill Grinder UI (`services/mill-grinder-ui`). The refresh modernizes the application appearance, improves code quality, and establishes consistent patterns across all views.

---

## Goals Achieved

- ✅ Modern, professional appearance with blue color palette
- ✅ Visual consistency across Chat, Data Model, and Context views
- ✅ Dark mode support with theme toggle
- ✅ Minimal header branding (title only, no logo)
- ✅ Eliminated code duplication in sidebar implementations
- ✅ Replaced workarounds with standard Mantine patterns
- ✅ Renamed "Concepts" to "Context" (navigation, URLs, files, icons)
- ✅ All icons from Tabler Icons (`react-icons/tb`)
- ✅ UI documentation created
- ✅ Basic smoke tests with Vitest

---

## Implementation Summary

### 1. Theme Configuration (`src/theme.ts`)

Extracted theme to dedicated file for testability:

```typescript
import { createTheme } from '@mantine/core';

export const theme = createTheme({
  primaryColor: 'primary',
  colors: {
    primary: [
      '#e7f5ff', '#d0ebff', '#a5d8ff', '#74c0fc', '#4dabf7',
      '#339af0', '#228be6', '#1c7ed6', '#1971c2', '#1864ab'
    ],
    dark: [
      '#C1C2C5', '#A6A7AB', '#909296', '#5c5f66', '#373A40',
      '#2C2E33', '#25262b', '#1A1B1E', '#141517', '#101113'
    ],
  },
  defaultRadius: 'md',
  shadows: {
    xs: '0 1px 2px rgba(0, 0, 0, 0.05)',
    sm: '0 1px 3px rgba(0, 0, 0, 0.1)',
    md: '0 4px 6px rgba(0, 0, 0, 0.1)',
  },
  components: {
    Card: { defaultProps: { shadow: 'xs', radius: 'md' } },
    Button: { defaultProps: { radius: 'md' } },
    NavLink: { defaultProps: { radius: 'sm' } },
  },
});
```

### 2. Layout Components Created

| Component | Location | Purpose |
|-----------|----------|---------|
| `AppHeader` | `src/component/layout/AppHeader.tsx` | Header with title, navbar toggle, theme toggle |
| `AppSidebar` | `src/component/layout/AppSidebar.tsx` | Shared sidebar with main navigation |
| `NavItem` | `src/component/layout/NavItem.tsx` | Reusable navigation link component |
| `ThemeToggle` | `src/component/layout/ThemeToggle.tsx` | Dark/light mode switch |

### 3. App Shell Structure (`src/App.tsx`)

```tsx
<AppShell
  header={{ height: 50 }}
  navbar={{ width: 300, breakpoint: 'sm', collapsed: {...} }}
  padding="md"
>
  <AppShell.Header>
    <AppHeader navbarOpened={navbarOpened} onToggleNavbar={toggleNavbar} />
  </AppShell.Header>
  
  <AppShell.Navbar p="xs">
    <AppSidebar>
      <SidebarContent /> {/* Route-specific content */}
    </AppSidebar>
  </AppShell.Navbar>
  
  <AppShell.Main style={{ height: 'calc(100vh - 50px)', overflow: 'hidden' }}>
    <Routes>...</Routes>
  </AppShell.Main>
</AppShell>
```

**Key**: `AppShell.Main` requires explicit height for nested ScrollAreas to work properly.

### 4. Concepts → Context Rename

| Before | After |
|--------|-------|
| `src/component/concepts/` | `src/component/context/` |
| `ConceptsLayout.tsx` | `ContextLayout.tsx` |
| `ConceptsView.tsx` | `ContextView.tsx` |
| `/concepts/:conceptId?` | `/context/:contextId?` |
| Icon: `TbBulb` | Icon: `TbFocusCentered` |

Redirect added for backward compatibility:
```tsx
<Route path="/concepts/:conceptId?" element={<Navigate to="/context" replace />} />
```

### 5. Sidebar Content Components

Created dedicated sidebar content components for each section:

- `ChatSidebarContent` - Chat list with create/delete actions
- `MetadataSidebarContent` - Schema/table navigation tree
- `ContextSidebarContent` - Context entity list

These are rendered dynamically based on the current route in `App.tsx`.

---

## Chat View Fixes

### Layout Chain for Proper Scrolling

The chat view required careful height management for scrolling to work:

```
AppShell.Main (height: calc(100vh - 50px), overflow: hidden)
└── ChatView Box (h="100%")
    └── ChatMessageList Stack (h="100%", overflow: hidden)
        └── Box (flex: 1, minHeight: 0, overflow: hidden)  ← Key fix!
            └── ScrollArea (h="100%", offsetScrollbars)
```

**Critical**: The `minHeight: 0` on the flex container is essential - flex items default to `min-height: auto` which prevents shrinking below content size.

### Auto-Scroll Implementation

```tsx
const bottomRef = useRef<HTMLDivElement>(null);

useEffect(() => {
  const timeoutId = setTimeout(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, 100);
  return () => clearTimeout(timeoutId);
}, [messages.list, messages.postingMessage]);

// In render:
<ScrollArea h="100%" w="100%" type="auto" offsetScrollbars scrollbarSize={8}>
  <Stack gap="sm" pr="sm">
    {messages.list.map(m => Message(m))}
    <Box ref={bottomRef} />  {/* Scroll anchor */}
  </Stack>
</ScrollArea>
```

### ScrollArea Configuration

| Prop | Value | Purpose |
|------|-------|---------|
| `type` | `"auto"` | Show scrollbar only when needed |
| `offsetScrollbars` | `true` | Prevent scrollbar from overlapping content |
| `scrollbarSize` | `8` | Smaller, cleaner scrollbar |
| `pr="sm"` (on content) | - | Add right padding for breathing room |

### Intent Cards

All intent cards updated to use full width:

```tsx
<Card w="100%" p="md" mb="xs">
  {/* Card content */}
</Card>
```

Components updated:
- `GetDataIntent.tsx`
- `ExplainIntent.tsx`
- `ClarificationMessage.tsx`
- `DoConversationIntent.tsx`
- `EnrichModelIntent.tsx`
- `UnsupportedIntent.tsx`
- `AssistantMessage.tsx`

---

## Data Table Fixes (`DataContainer.tsx`)

```tsx
<ScrollArea
  scrollbars="xy"
  type="always"         // Always show scrollbars for data tables
  offsetScrollbars={true}
  scrollbarSize={12}
  mah="60vh"           // Use viewport height for responsive sizing
  style={{ width: "100%" }}
>
```

**Key**: Using `mah="60vh"` instead of percentage ensures the table respects viewport size when browser dev tools are opened.

---

## Data Model View (`EntityDetails.tsx`)

### Entity Header Card

The header card displays entity information with structural facet merged inline:

```tsx
<Card withBorder>
  <Group justify="space-between" align="flex-start">
    <Stack gap="xs" style={{ flex: 1 }}>
      {/* Entity name, type badge */}
      <Group gap="sm">
        {getTypeIcon()}
        <Text fw={600} size="lg">{selected.id}</Text>
        <Badge variant="light" color="primary">{selected.type}</Badge>
      </Group>
      
      {/* Location */}
      <Group gap="xs">
        <Text size="sm" c="dimmed">Location:</Text>
        <Code>{location}</Code>
      </Group>
      
      {/* Structural info - merged into header */}
      <Group gap="xs" wrap="wrap">
        <Badge variant="outline" size="xs" color="blue">{formatType()}</Badge>
        <Badge variant="filled" color="yellow" size="xs" leftSection={<TbKey />}>PK</Badge>
        <Badge variant="filled" color="red" size="xs">Not Null</Badge>
        {/* ... more structural badges */}
      </Group>
    </Stack>
    
    {/* Scope selector - top right */}
    <Select label="Scope" size="xs" w={180} />
  </Group>
</Card>
```

### Facet Container Styling

All facets use consistent transparent background with border:

```tsx
const facetBoxStyle = {
  borderRadius: 'var(--mantine-radius-md)',
  border: '1px solid var(--mantine-color-gray-3)'
};

// Usage
<Box p="sm" style={facetBoxStyle}>
  <Text size="xs" fw={500} c="dimmed" mb="xs">Description</Text>
  <FacetViewer ... />
</Box>
```

### Facet Layout Structure

```
┌─ Entity Header (Card withBorder) ──────────────────────┐
│ 🗃️ entity_id  [TABLE]                      [Scope ▼]  │
│ Location: schema.table                                 │
│ [VARCHAR(255)] [PK] [Not Null] 📁 PostgreSQL           │
└────────────────────────────────────────────────────────┘

┌─ Description (Box with border) ────────────────────────┐
│ Display name, description, business meaning            │
│ [Domain] [Owner] [Classification] [Tags]               │
└────────────────────────────────────────────────────────┘

┌─ [relation] [concept] (Box with border + Tabs) ────────┐
│ Tabbed content for remaining facets                    │
└────────────────────────────────────────────────────────┘

┌─ Related Items (Box with border) ──────────────────────┐
│ 🗃️ Tables [3]                                          │
│   table1 · table2 · table3                             │
└────────────────────────────────────────────────────────┘
```

### Relation Facet View

Relations displayed as compact, expandable list items:

```tsx
<UnstyledButton
  w="100%"
  style={{ 
    borderRadius: 'var(--mantine-radius-sm)',
    border: '1px solid var(--mantine-color-gray-2)',
  }}
  styles={{
    root: {
      '&:hover': {
        backgroundColor: 'var(--mantine-color-gray-0)',
        borderColor: 'var(--mantine-color-gray-4)',
      },
    },
  }}
>
  <Group gap="sm" p="xs" wrap="nowrap" justify="space-between">
    {/* Relation icon for visual accent */}
    <TbArrowsJoin2 size={16} style={{ color: 'var(--mantine-color-primary-5)' }} />
    
    {/* Source → Target */}
    <Group gap={4}>
      <TbKey size={12} />
      <Text size="xs" fw={500}>orders</Text>
      <TbArrowRight size={14} />
      <TbLink size={12} />
      <Text size="xs" fw={500}>customers</Text>
    </Group>
    
    {/* Cardinality and type badges */}
    <Group gap={4}>
      <Badge variant="light" color="green" size="xs">1:N</Badge>
      <Badge variant="outline" color="red" size="xs">FK</Badge>
    </Group>
  </Group>
</UnstyledButton>
```

**Expanded details** (click to expand):
```tsx
<Box 
  ml="xs" mt="xs" p="sm" 
  style={{ 
    borderRadius: 'var(--mantine-radius-sm)',
    border: '1px solid var(--mantine-color-gray-3)',
  }}
>
  <Stack gap="sm">
    <Box>
      <Text size="xs" fw={500} c="dimmed" mb={4}>Path</Text>
      <Group gap="xs">
        <Badge variant="light" color="blue">schema.source_table</Badge>
        <TbArrowRight />
        <Badge variant="light" color="green">schema.target_table</Badge>
      </Group>
    </Box>
    <Box>
      <Text size="xs" fw={500} c="dimmed" mb={4}>Description</Text>
      <Text size="sm">Relation description here</Text>
    </Box>
    {/* Business Meaning, Join SQL */}
  </Stack>
</Box>
```

### Related Items Component

```tsx
const boxStyle = { 
  borderRadius: 'var(--mantine-radius-md)', 
  border: '1px solid var(--mantine-color-gray-3)' 
};

<Box p="sm" style={boxStyle}>
  <Text size="xs" fw={500} c="dimmed" mb="xs">Related Items</Text>
  <Stack gap="xs">
    {/* Grouped by type */}
    <Box>
      <Group gap={4} mb={4}>
        <TbTable size={14} />
        <Text size="xs" c="dimmed">Tables</Text>
        <Badge variant="light" size="xs" color="gray">3</Badge>
      </Group>
      <Stack gap={2}>
        <RelatedItemLink entity={...} />
      </Stack>
    </Box>
  </Stack>
</Box>
```

### Consistent Styling Summary

| Element | Style |
|---------|-------|
| Header Card | `<Card withBorder>` |
| Facet containers | `border: '1px solid var(--mantine-color-gray-3)'`, `borderRadius: 'var(--mantine-radius-md)'` |
| Relation items | Individual borders, icon prefix, hover effect |
| Related Items | Same border style as facets |
| Section labels | `<Text size="xs" fw={500} c="dimmed" mb="xs">` |
| Badges | `size="xs"`, appropriate color variants |

---

## Testing Setup

### Configuration (`vite.config.ts`)

```typescript
/// <reference types="vitest/config" />
import { defineConfig } from 'vitest/config';

export default defineConfig({
  // ... existing config
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
  },
});
```

### Test Setup (`src/test/setup.ts`)

```typescript
import '@testing-library/jest-dom';

// Mock matchMedia for Mantine components
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => true,
  }),
});

// Mock ResizeObserver for Mantine components
window.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
};
```

### Test Wrapper (`src/test/TestWrapper.tsx`)

```tsx
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { theme } from '../theme';

export function TestWrapper({ children }: { children: React.ReactNode }) {
  return (
    <MemoryRouter>
      <MantineProvider theme={theme}>
        {children}
      </MantineProvider>
    </MemoryRouter>
  );
}
```

### NPM Scripts

```json
{
  "scripts": {
    "test": "vitest",
    "test:run": "vitest run"
  }
}
```

---

## Tabler Icons Reference

| Component | Icon | Import |
|-----------|------|--------|
| Chat nav | `TbTerminal2` | `react-icons/tb` |
| Data Model nav | `TbCompass` | `react-icons/tb` |
| Context nav | `TbFocusCentered` | `react-icons/tb` |
| Theme toggle (light) | `TbMoon` | `react-icons/tb` |
| Theme toggle (dark) | `TbSun` | `react-icons/tb` |
| Navbar toggle | `←` / `→` | Text characters |
| Not found | `TbFileOff` | `react-icons/tb` |
| Schema type | `TbDatabase` | `react-icons/tb` |
| Table type | `TbTable` | `react-icons/tb` |
| Attribute type | `TbColumns` | `react-icons/tb` |
| Concept type | `TbBulb` | `react-icons/tb` |
| Primary Key | `TbKey` | `react-icons/tb` |
| Foreign Key | `TbLink` | `react-icons/tb` |
| Unique constraint | `TbShieldCheck` | `react-icons/tb` |
| Relation item | `TbArrowsJoin2` | `react-icons/tb` |
| Relation arrow | `TbArrowRight` | `react-icons/tb` |
| Cardinality | `TbHierarchy2` | `react-icons/tb` |
| Business domain | `TbBuilding` | `react-icons/tb` |
| Business owner | `TbUser` | `react-icons/tb` |
| Classification | `TbShield` | `react-icons/tb` |
| Unit | `TbRuler` | `react-icons/tb` |
| Tags | `TbTag` | `react-icons/tb` |

---

## Component Hierarchy

```
App.tsx
├── AppShell.Header
│   └── AppHeader
│       ├── Burger (mobile)
│       ├── Text "Mill Grinder"
│       ├── ActionIcon (navbar toggle, desktop)
│       └── ThemeToggle
├── AppShell.Navbar
│   └── AppSidebar
│       ├── NavItem (Chat)
│       ├── NavItem (Data Model)
│       ├── NavItem (Context)
│       ├── Divider
│       └── ScrollArea
│           └── SidebarContent (route-specific)
│               ├── ChatSidebarContent
│               ├── MetadataSidebarContent
│               └── ContextSidebarContent
└── AppShell.Main
    └── Routes
        ├── /chat/* → ChatLayout → ChatView
        │   └── ChatMessageListRender
        │       ├── ScrollArea (messages)
        │       └── ChatPostMessage
        ├── /data-model/* → MetadataLayout
        └── /context/* → ContextLayout
```

---

## Files Changed

### Created (Layout)
- `src/component/layout/AppHeader.tsx`
- `src/component/layout/AppSidebar.tsx`
- `src/component/layout/NavItem.tsx`
- `src/component/layout/ThemeToggle.tsx`
- `src/theme.ts`

### Created (Sidebar Content)
- `src/component/chat/ChatSidebarContent.tsx`
- `src/component/data-model/MetadataSidebarContent.tsx`
- `src/component/context/ContextSidebarContent.tsx`

### Created (Testing)
- `src/test/setup.ts`
- `src/test/TestWrapper.tsx`
- `src/component/layout/AppHeader.test.tsx`

### Created (Documentation)
- `src/README.md`

### Renamed (Concepts → Context)
- `src/component/concepts/` → `src/component/context/`
- `ConceptsLayout.tsx` → `ContextLayout.tsx`
- `ConceptsView.tsx` → `ContextView.tsx`

### Modified (Core)
- `src/main.tsx` - Import theme from separate file
- `src/App.tsx` - AppShell structure, route updates
- `vite.config.ts` - Vitest configuration
- `package.json` - Test dependencies and scripts

### Modified (Chat)
- `src/component/chat/ChatMessageList.tsx` - Layout fixes, auto-scroll
- `src/component/chat/ChatView.tsx` - Simplified structure
- `src/component/chat/intents/*.tsx` - Full width cards, consistent styling

### Modified (Data)
- `src/component/data/DataContainer.tsx` - ScrollArea fixes

### Modified (Data Model)
- `src/component/data-model/EntityDetails.tsx` - Restructured layout, structural facet merged into header, single scope selector
- `src/component/data-model/FacetViewer.tsx` - Added hideScope prop
- `src/component/data-model/components/RelatedItems.tsx` - Compact list style with borders
- `src/component/data-model/components/RelatedItemLink.tsx` - Simplified link styling
- `src/component/data-model/components/facets/BaseFacetViewer.tsx` - Conditional scope display
- `src/component/data-model/components/facets/FacetViewerRouter.tsx` - Pass hideScope prop
- `src/component/data-model/components/facets/DescriptiveFacetView.tsx` - Compact layout
- `src/component/data-model/components/facets/StructuralFacetView.tsx` - Compact layout (now merged into header)
- `src/component/data-model/components/facets/RelationFacetView.tsx` - Compact list with expandable details, visual accents

### Modified (Navigation)
- `src/component/data-model/components/TopNavigation.tsx` - Context rename

### Modified (Tree Components)
- Removed manual hover handlers, using Mantine defaults

---

## Best Practices Established

### 1. Use Mantine Style Props
```tsx
// ✅ Good
<Box p="md" mt="sm" radius="md">

// ❌ Avoid
<Box style={{ padding: 16, marginTop: 8, borderRadius: 8 }}>
```

### 2. Use Mantine's styles API for Hover
```tsx
// ✅ Good
<NavLink styles={{ root: { '&:hover': { bg: 'gray.1' } } }} />

// ❌ Avoid
<NavLink onMouseEnter={...} onMouseLeave={...} />
```

### 3. Use Theme Tokens
```tsx
// ✅ Good
<Card radius="md" shadow="sm">

// ❌ Avoid
<Card style={{ borderRadius: 8, boxShadow: '...' }}>
```

### 4. Flex Container Height Fix
```tsx
// ✅ Required for ScrollArea in flex containers
<Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
  <ScrollArea h="100%">...</ScrollArea>
</Box>
```

### 5. ScrollArea Best Practices
```tsx
<ScrollArea
  type="auto"           // or "always" for data tables
  offsetScrollbars      // Prevent content overlap
  scrollbarSize={8}     // Cleaner appearance
>
  <Content pr="sm" />   // Add padding for breathing room
</ScrollArea>
```

### 6. Consistent Container Styling
```tsx
// ✅ Standard facet/section container
const boxStyle = {
  borderRadius: 'var(--mantine-radius-md)',
  border: '1px solid var(--mantine-color-gray-3)'
};

<Box p="sm" style={boxStyle}>
  <Text size="xs" fw={500} c="dimmed" mb="xs">Section Label</Text>
  {/* Content */}
</Box>

// ✅ List item with hover
<UnstyledButton
  style={{ 
    borderRadius: 'var(--mantine-radius-sm)',
    border: '1px solid var(--mantine-color-gray-2)',
  }}
  styles={{
    root: {
      '&:hover': {
        backgroundColor: 'var(--mantine-color-gray-0)',
        borderColor: 'var(--mantine-color-gray-4)',
      },
    },
  }}
>
```

### 7. Badge Conventions
```tsx
// Type badges
<Badge variant="light" color="primary">TABLE</Badge>

// Constraint badges  
<Badge variant="filled" color="yellow" size="xs" leftSection={<TbKey size={10} />}>PK</Badge>
<Badge variant="filled" color="orange" size="xs" leftSection={<TbLink size={10} />}>FK</Badge>
<Badge variant="filled" color="red" size="xs">Not Null</Badge>

// Data type badges
<Badge variant="outline" size="xs" color="blue">VARCHAR(255)</Badge>

// Cardinality badges
<Badge variant="light" color="green" size="xs" leftSection={<TbHierarchy2 size={10} />}>1:N</Badge>
```

---

## Running Tests

```bash
# Run tests in watch mode
npm test

# Run tests once
npm run test:run

# Run specific test file
npm test -- AppHeader
```

---

## Development Commands

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```
