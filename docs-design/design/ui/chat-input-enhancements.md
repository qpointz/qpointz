# Chat Input Enhancements: Command Palette and @ Mentions

## Overview

This document describes the command palette and @ mention features added to the chat input component (`PostMessage.tsx`). These enhancements allow users to quickly access intents and reference metadata entities (tables and attributes) directly from the chat input.

## Features Implemented

### 1. Command Palette (`/` prefix)

Users can type `/` at the start of the input to access a command palette with available intents.

#### Behavior
- **Trigger**: Typing `/` at the beginning of the input or after whitespace
- **Menu Display**: Shows a compact dropdown menu above the input with available commands
- **Filtering**: As user types after `/`, commands are filtered by:
  - Command ID (e.g., `get-data`)
  - Command label (e.g., "Get Data")
  - Command description
- **Keyboard Navigation**:
  - `Arrow Up/Down`: Navigate through commands
  - `Enter`: Select command and insert `/<command-id> ` into input
  - `Escape`: Cancel and clear the input
- **Mouse Support**: Hover to highlight, click to select

#### Available Commands
- `get-data` - Retrieve tabular data
- `get-chart` - Visualize data as chart
- `explain` - Explain query or result
- `refine` - Modify previous query
- `do-conversation` - Casual conversation
- `enrich-model` - Add domain knowledge

#### Implementation Details
- Commands are defined as a constant array with icons from `react-icons/tb`
- Filtering is case-insensitive and uses `useMemo` for performance
- Selected index resets when filtered results change
- Menu appears above input with compact styling (4px padding, xs text, 200px max height)

### 2. @ Mentions (`@` prefix)

Users can type `@` to search and reference metadata entities (tables and attributes).

#### Behavior
- **Trigger**: Typing `@` at word boundary (start of input or after whitespace)
- **Query Extraction**: Text after `@` up to next space is used as search query
- **Search**: Debounced (300ms) API calls to `SchemaExplorerApi.search()` for:
  - Tables (type: `TABLE`)
  - Attributes (type: `ATTRIBUTE`)
- **Menu Display**: Shows search results in a compact dropdown above input
- **Error Handling**: Uses `Promise.allSettled` to handle individual search failures gracefully
- **Keyboard Navigation**:
  - `Arrow Up/Down`: Navigate through results
  - `Enter`: Select entity and insert `@entity-name ` into input
  - `Escape`: Cancel and remove `@` and query from input
- **Mouse Support**: Hover to highlight, click to select

#### Search Behavior
- Empty query: No search performed (avoids API errors)
- Non-empty query: Searches both tables and attributes simultaneously
- Results sorted by relevance score (if available), then alphabetically
- Limited to 20 results for performance
- Individual search failures (e.g., ATTRIBUTE search returning 500) don't break the feature - successful results are still shown

#### Result Display
Each result shows:
- Icon (table icon for tables, columns icon for attributes)
- Display name or entity name
- Description (if available, truncated to 1 line)
- Location information (if available)

#### Implementation Details
- Uses `SchemaExplorerApi` from metadata service
- Debounced search with 300ms delay to avoid excessive API calls
- Handles both array and single object responses from API
- Cursor position is set correctly after insertion
- Menu styling matches command palette (compact, 4px padding, xs text, 200px max height)

## UI/UX Design

### Compact Menu Styling
Both menus use consistent compact styling:
- **Padding**: 4px
- **Gap between items**: 1px
- **Icon size**: 14px
- **Text size**: xs (12px)
- **Max height**: 200px with scroll
- **Border radius**: 8px
- **Shadow**: md
- **Z-index**: 1000 (appears above other content)

### Visual Feedback
- Selected item highlighted with primary color background
- Border changes on selection
- Smooth hover transitions
- Loading indicator for @ mentions (compact, xs size)

### Accessibility
- Keyboard navigation support
- Visual focus indicators
- Clear visual hierarchy
- Truncated text with ellipsis to prevent overflow

## Technical Implementation

### File Modified
- `services/mill-grinder-ui/src/component/chat/PostMessage.tsx`

### Dependencies Added
- `SchemaExplorerApi` from `../../api/mill/api.ts`
- `Configuration` from `../../api/mill`
- Additional icons: `TbTable`, `TbColumns` from `react-icons/tb`

### State Management
- `selectedCommandIndex`: Tracks selected command in palette
- `selectedMentionIndex`: Tracks selected entity in mention menu
- `mentionResults`: Stores search results
- `mentionLoading`: Loading state for search
- `searchTimeoutRef`: Reference for debounce timeout

### Key Functions
- `findMentionStart()`: Finds `@` at word boundary
- `searchMetadata()`: Performs debounced search with error handling
- `getEntityIcon()`: Returns appropriate icon for entity type
- `getEntityDisplayName()`: Extracts display name from search result
- `handleCtrlEnterTextArea()`: Enhanced to handle both command and mention navigation

## Error Handling

### Command Palette
- No API calls, so no error handling needed

### @ Mentions
- Empty query: Skipped (no API call)
- Individual search failures: Handled with `Promise.allSettled`
  - Failed searches logged as warnings
  - Successful searches still show results
  - User experience not broken by partial failures
- Network errors: Caught and logged, empty results shown

## Future Enhancements

Potential improvements:
1. **Caching**: Cache search results to reduce API calls
2. **Recent items**: Show recently used commands/entities
3. **Fuzzy search**: Improve search matching algorithm
4. **Keyboard shortcuts**: Add more keyboard shortcuts (e.g., Tab to complete)
5. **Entity preview**: Show more details in hover tooltip
6. **Multi-select**: Allow selecting multiple entities
7. **Command arguments**: Support command parameters (e.g., `/get-data limit=10`)

## Testing Considerations

- Test command palette with various filter queries
- Test @ mentions with different entity names
- Test keyboard navigation in both menus
- Test error scenarios (API failures, network issues)
- Test edge cases (very long names, special characters)
- Test on different screen sizes
- Test with empty metadata (no tables/attributes)

## Related Files

- `services/mill-grinder-ui/src/component/chat/PostMessage.tsx` - Main implementation
- `services/mill-grinder-ui/src/api/mill/api.ts` - API client definitions
- `services/mill-grinder-ui/src/component/chat/ChatProvider.tsx` - Chat context (used for posting messages)

## Branch

Implemented on `prototype/experiment` branch for easy testing and potential reversion.
