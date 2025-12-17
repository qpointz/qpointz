# Mill Grinder UI - Source Documentation

## Overview

Mill Grinder UI is a React + TypeScript single-page application built with Vite and Mantine UI library.
It provides a chat interface, data model explorer, and context management for the Mill platform.

## Technology Stack

- **React 19** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Mantine 8** - UI component library
- **React Router 7** - Client-side routing
- **Vitest** - Testing framework
- **Tabler Icons** - Icon library (`react-icons/tb`)

## Component Structure

```
src/
├── component/
│   ├── layout/           # Shared layout components
│   │   ├── AppHeader     # Header with logo + theme toggle
│   │   ├── AppSidebar    # Reusable sidebar wrapper with navigation
│   │   ├── NavItem       # Navigation link component
│   │   └── ThemeToggle   # Dark/light mode switch
│   │
│   ├── chat/             # Chat feature
│   │   ├── ChatLayout    # Route wrapper for chat
│   │   ├── ChatView      # Main chat view (messages or new chat)
│   │   ├── ChatProvider  # Chat state management (context)
│   │   ├── ChatList      # (legacy) Chat sidebar list
│   │   ├── ChatSidebarContent  # Sidebar content for chat routes
│   │   ├── ChatMessageList     # Message display
│   │   ├── BeginNewChat        # New chat form
│   │   ├── PostMessage         # Message input
│   │   └── intents/            # Message type renderers
│   │
│   ├── data-model/       # Data model explorer
│   │   ├── MetadataLayout      # Main layout for data model
│   │   ├── MetadataProvider    # Metadata state management
│   │   ├── MetadataSidebar     # (legacy) Sidebar wrapper
│   │   ├── MetadataSidebarContent  # Sidebar content for data-model routes
│   │   ├── EntityDetails       # Entity detail view
│   │   ├── FacetViewer         # Facet display
│   │   ├── components/         # Reusable sub-components
│   │   │   ├── tree/           # Tree navigation (schemas, tables)
│   │   │   ├── facets/         # Facet viewers
│   │   │   └── ...
│   │   └── utils/              # Entity parsing utilities
│   │
│   ├── context/          # Context management (renamed from concepts)
│   │   ├── ContextLayout       # Main layout for context
│   │   ├── ContextView         # Context list/filter view
│   │   └── ContextSidebarContent  # Sidebar content for context routes
│   │
│   ├── data/             # Data display components
│   │   ├── ChartView     # ECharts wrapper
│   │   └── DataContainer # Table data display
│   │
│   └── NotFound.tsx      # 404 page
│
├── api/mill/             # Generated API client (do not edit manually)
│
├── test/                 # Test infrastructure
│   ├── setup.ts          # Vitest setup with mocks
│   └── TestWrapper.tsx   # Test provider wrapper
│
├── theme.ts              # Mantine theme configuration
├── main.tsx              # App entry point
├── App.tsx               # Root component with AppShell
└── index.css             # Global styles (minimal)
```

## Layout System

The app uses Mantine's `AppShell` for layout management:

```tsx
<AppShell
  header={{ height: 50 }}
  navbar={{ width: 300, breakpoint: 'sm', collapsed: {...} }}
>
  <AppShell.Header>
    <AppHeader />  // Logo + theme toggle + navbar toggle
  </AppShell.Header>
  
  <AppShell.Navbar>
    <AppSidebar>
      <SidebarContent />  // Route-specific content
    </AppSidebar>
  </AppShell.Navbar>
  
  <AppShell.Main>
    <Routes>...</Routes>
  </AppShell.Main>
</AppShell>
```

### Key Benefits
- Automatic height management (no 100vh workarounds)
- Built-in responsive behavior
- Consistent sidebar collapse across all views

## Theme Configuration

Theme is defined in `src/theme.ts`:

- **Primary Color**: Professional blue palette
- **Dark Mode**: Full support via `useMantineColorScheme()`
- **Default Radius**: `md` for consistent rounded corners
- **Component Defaults**: Card shadows, button radius, NavLink radius

### Color Scheme Toggle

```tsx
import { useMantineColorScheme } from '@mantine/core';

const { colorScheme, toggleColorScheme } = useMantineColorScheme();
```

## Routing

| Path | Component | Description |
|------|-----------|-------------|
| `/chat/*` | ChatLayout | Chat interface |
| `/data-model/:schema?/:table?/:attribute?` | MetadataLayout | Data model explorer |
| `/context/:contextId?` | ContextLayout | Context management |
| `/` | Redirect to /chat | Default route |

## API Integration

Generated OpenAPI client is located at `src/api/mill/`.
**Do not edit these files manually** - they are regenerated from the OpenAPI spec.

Usage:
```tsx
import { NlSqlChatControllerApi, Configuration } from '../api/mill';

const api = new NlSqlChatControllerApi(new Configuration());
const response = await api.listChats();
```

## Testing

### Run Tests
```bash
npm test          # Watch mode
npm run test:run  # Single run
```

### Test Structure
Tests use Vitest + React Testing Library. Use `TestWrapper` for Mantine/Router context:

```tsx
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../test/TestWrapper';

render(<MyComponent />, { wrapper: TestWrapper });
```

## Icon Usage

All icons use Tabler Icons (`react-icons/tb`):

| Component | Icon | Description |
|-----------|------|-------------|
| Chat nav | `TbTerminal2` | Chat/conversation |
| Data Model nav | `TbCompass` | Explore data model |
| Context nav | `TbFocusCentered` | Context focus |
| Theme toggle | `TbSun` / `TbMoon` | Light/dark mode |
| Not found | `TbFileOff` | Missing page |

## Development

```bash
npm run dev       # Start dev server
npm run build     # Production build
npm run lint      # ESLint check
npm run preview   # Preview production build
```

## Best Practices

1. **Use Mantine props** instead of inline styles:
   ```tsx
   // Good
   <Box p="md" mt="sm" radius="md">
   
   // Avoid
   <Box style={{ padding: 16, marginTop: 8, borderRadius: 8 }}>
   ```

2. **Use NavLink** for navigation items - it handles hover states automatically

3. **Use theme tokens** (`xs`, `sm`, `md`, `lg`, `xl`) for spacing and sizes

4. **Keep providers minimal** - providers like MetadataProvider should wrap only the components that need them
