---
name: Update Clarification UI Flow
overview: Refactor the clarification flow to automatically activate clarification mode when a clarification request is received, add a single-line indicator above the input field showing the initial question, replace multiple Reply buttons with a single Reply/Answer button, and make reasoning-id scoped to the clarification context rather than global state.
todos:
  - id: update-chat-provider-state
    content: Refactor ChatProvider to use clarification context object (reasoningId + initialQuestion) instead of global activeReasoningId
    status: pending
  - id: auto-activate-clarification
    content: Update SSE handler and initial load effect to automatically activate clarification mode when clarification message arrives
    status: pending
  - id: extract-initial-question
    content: Implement logic to extract initial user question from message history when clarification arrives
    status: pending
  - id: update-clarification-message-ui
    content: Replace multiple Reply buttons with single Reply/Answer button in ClarificationMessage component
    status: pending
  - id: create-status-indicator-component
    content: Create reusable StatusIndicator component supporting mode (clarification) and event feedback (thinking/executing) independently, with show(message, durationSeconds?) and hide() API
    status: pending
  - id: implement-event-notification-api
    content: Implement show/hide methods with auto-hide timeout functionality using useState, useEffect, and useImperativeHandle
    status: pending
  - id: integrate-status-indicator
    content: Integrate StatusIndicator in PostMessage component with clarification mode props and expose ref/API for future SSE event integration
    status: pending
  - id: update-message-input-styling
    content: Update PostMessage textarea styling to be smaller and more ChatGPT-style (compact, subtle borders, reduced height)
    status: pending
  - id: remove-prefill-logic
    content: Remove or simplify prefill text logic since clarification mode no longer pre-fills input
    status: pending
  - id: update-context-interface
    content: Update ChatContextType interface to reflect new clarification context structure
    status: pending
---

# Update Clarification UI Flow

## Overview

Refactor the clarification mechanism to automatically activate when clarification requests arrive, display a clarification indicator above the input field, and scope reasoning-id to the active clarification context.

## Current Flow Analysis

The current implementation in [`ChatProvider.tsx`](services/mill-grinder-ui/src/component/chat/ChatProvider.tsx) manages `activeReasoningId` as global state:

- Set when SSE receives CHAT message with `need-clarification: true` and `reasoning-id` (lines 229-239)
- Set on initial load from last CHAT message (lines 249-265)
- Cleared when clarification is cancelled or resolved
- Included in message content when posting (line 138)

[`ClarificationMessage.tsx`](services/mill-grinder-ui/src/component/chat/intents/ClarificationMessage.tsx) displays all questions with individual Reply buttons that call `onReply(question)` to prefill the input.

## Changes Required

### 1. Update ChatProvider Clarification State

**File**: `services/mill-grinder-ui/src/component/chat/ChatProvider.tsx`

- Replace global `activeReasoningId` state with a clarification context object containing:
  - `reasoningId?: string`
  - `initialQuestion?: string` (user's original message that triggered clarification)
  - Auto-activate when clarification message arrives (SSE or initial load)

- Update SSE handler (lines 229-240):
  - When `need-clarification: true` and `reasoning-id` present, extract and store:
    - `reasoningId` from `content['reasoning-id']`
    - `initialQuestion` from previous USER message in the message list (or from clarification message if available)

- Update initial load effect (lines 249-265):
  - Similar logic: find last CHAT message with clarification, extract reasoning-id and initial question

- Update `messagePost` (line 138):
  - Include `reasoning-id` in content only if clarification context is active

- Update `handleClarificationCancel` (line 171):
  - Clear entire clarification context (not just reasoning-id)

- Remove `handleClarificationReply` prefill logic (line 163-166) or simplify to just ensure clarification mode is active

- Update context interface (line 44-49):
  - Change `activeReasoningId?: string` to clarification object with reasoningId and initialQuestion
  - Update `reply` callback signature if needed

### 2. Update ClarificationMessage Component

**File**: `services/mill-grinder-ui/src/component/chat/intents/ClarificationMessage.tsx`

- Remove per-question Reply buttons (lines 40-51)
- Add single "Reply" or "Answer" button that calls `onReply()` without parameters (or with empty string)
- Keep all questions displayed as list items
- Keep "Start Over" cancel button functionality

### 3. Create Reusable Status Indicator Component

**New File**: `services/mill-grinder-ui/src/component/chat/StatusIndicator.tsx`

Create a reusable component that supports two independent display modes:

- **Mode Indicator**: Shows contextual mode (e.g., "clarification mode") with optional action button
- **Event Feedback**: Shows transient status updates from SSE (e.g., "thinking", "executing")

Design requirements:

- Position: Above textarea input in PostMessage
- Clear visual indication with color coding
- Compact single-line layout
- Support displaying mode and event feedback independently or together
- Mode indicator: Shows mode name + optional text (e.g., "clarify: <question>") + cancel button
- Event feedback: Shows status text (e.g., "thinking...", "executing query...") with optional icon/spinner

Component structure:

```typescript
interface StatusIndicatorProps {
    mode?: {
        type: 'clarification' | string; // extensible for future modes
        label: string; // e.g., "clarify: <initialQuestion>"
        onCancel?: () => void; // optional cancel handler
    };
    // Note: Event feedback is controlled via ref API (show/hide methods), not props
}

// For programmatic control of event notifications (exposed via ref)
interface StatusIndicatorRef {
    show: (message: string, durationSeconds?: number) => void; // Show notification, auto-hide after duration (in seconds)
    hide: () => void; // Hide notification immediately
}
```

Styling:

- Mode indicator: Distinct color (e.g., blue/amber background) with clear visual distinction
- Event feedback: Different styling (e.g., subtle gray background with animated indicator)
- Both visible: Stack vertically or use separator
- Responsive: Truncate long text appropriately

### 4. Integrate Status Indicator in PostMessage

**File**: `services/mill-grinder-ui/src/component/chat/PostMessage.tsx`

- Import and use `StatusIndicator` component above textarea
- Pass clarification mode props when clarification context is active:
  - `mode={{ type: 'clarification', label: 'clarify: <initialQuestion>', onCancel: clarification.cancel }}`
- For event feedback (future enhancement, not props-based):
  - Use ref to access `show(message, durationSeconds?)` and `hide()` methods programmatically
  - Example: `statusIndicatorRef.current.show("Executing query...", 10)` - shows notification, auto-hides after 10 seconds
  - Example: `statusIndicatorRef.current.hide()` - immediately hides notification
- Component should handle both being present simultaneously

**Event Feedback API Integration**:

- StatusIndicator should expose `show(message, durationSeconds?)` and `hide()` methods via ref
- These methods can be called programmatically from ChatProvider or other components
- `show()` displays the notification and auto-hides after specified seconds (or stays until `hide()` is called)
- `hide()` immediately hides the current notification
- Implementation should use useEffect to handle auto-hide timeout with cleanup

**Update Message Input Styling to ChatGPT Style**:

**File**: `services/mill-grinder-ui/src/component/chat/PostMessage.tsx`

Make textarea input smaller and more compact (ChatGPT-style). Current implementation uses:

- Box container: `h={140}`, `mb={100}`, `p={10}`, `bg="white"`, `borderRadius: 10`
- Textarea: `minRows={5}`, `maxRows={5}`, `rows={5}`, `variant="unstyled"`, `h="100%"`

Updates needed:

- Reduce overall container height - make it more compact (ChatGPT uses a single-line input that expands)
- Reduce textarea rows: Change from `rows={5}` to `minRows={1}` or `minRows={2}` with `maxRows={4}` or `maxRows={5}` for expansion
- Update styling to be more subtle:
  - Remove or reduce heavy white background
  - Use subtle border instead (e.g., `border: 1px solid`) with light gray color
  - Add subtle shadow for depth (like ChatGPT's floating input bar)
  - Reduce padding for more compact appearance
  - Use rounded corners (keep borderRadius but adjust)
- Make send button more integrated:
  - Position it better within the input area (right-aligned, possibly inside the border)
  - Make it slightly larger/more prominent
  - Consider different icon or styling
- Ensure autosize still works for multi-line expansion
- Adjust margins/spacing to be more compact (reduce `mb={100}` significantly)

**ChatGPT-style characteristics to emulate**:

- Single line that expands when user types multiple lines
- Subtle border with rounded corners
- Clean, minimal appearance
- Send button integrated into the input area
- Compact vertical spacing

### 5. Remove Prefill Logic

**File**: `services/mill-grinder-ui/src/component/chat/PostMessage.tsx`

- Remove or simplify prefill effect (lines 43-57) since clarification mode no longer pre-fills input
- Ensure input field value doesn't change when clarification is activated

### 6. Update ChatMessageList Integration

**File**: `services/mill-grinder-ui/src/component/chat/ChatMessageList.tsx`

- Verify `ClarificationMessage` receives correct props
- Ensure `onReply` callback works with new single-button design

## Implementation Details

### Finding Initial Question

When a clarification message arrives, extract the user's original question by:

1. Looking for previous USER message in the message list (most recent before the clarification CHAT message)
2. Or checking if clarification message content includes reference to original question
3. Fallback to empty string if not found

### State Structure

```typescript
interface ClarificationContext {
    reasoningId?: string;
    initialQuestion?: string; // User's original message
}
```

### Status Indicator Component Design

**Location**: `services/mill-grinder-ui/src/component/chat/StatusIndicator.tsx`

**Visual Design**:

- **Mode Indicator**: 
  - Background color: Distinct (e.g., blue/amber) to clearly indicate mode is active
  - Layout: Single line with mode label on left, cancel button on right
  - Text format: "clarify: {initialQuestion}" (truncate if too long with ellipsis)
  - Cancel button: X icon or "Cancel" text, positioned on right

- **Event Feedback**:
  - Background color: Subtle (e.g., light gray) to indicate transient status
  - Layout: Single line with optional icon/spinner on left, message text
  - Animation: Optional subtle animation or spinner for active states

- **When Both Present**:
  - Stack vertically with mode indicator on top
  - Or use separator/divider between them
  - Ensure both are clearly visible

**Future SSE Event Support** (not in current scope, but component designed for it):

- Component exposes `show(message, durationSeconds?)` and `hide()` API for programmatic control
- Future implementation could:
  - Listen for additional SSE event types (e.g., "thinking_event", "executing_event") in ChatProvider
  - Call `statusIndicatorRef.current.show("Executing query...", 5)` when events are received
  - Automatically handle timeouts or call `hide()` when events complete
- StatusIndicator will display these events independently or alongside mode indicators
- Example usage: `statusIndicator.show("Thinking...", 10)` - shows notification, auto-hides after 10 seconds

**Component API**:

```typescript
export interface StatusMode {
    type: 'clarification' | string; // extensible
    label: string;
    onCancel?: () => void;
}

export interface StatusIndicatorProps {
    mode?: StatusMode;
    // Note: Event feedback is controlled via ref API (show/hide methods), not props
}

// For programmatic control of event notifications (exposed via ref)
export interface StatusIndicatorRef {
    show: (message: string, durationSeconds?: number) => void;
    hide: () => void;
}

// Component should forward ref for programmatic control
export const StatusIndicator = forwardRef<StatusIndicatorRef, StatusIndicatorProps>(...)
```

**Implementation Notes**:

- Use Mantine components (Box, Group, Text, Button, ActionIcon, Badge, Loader) for consistency
- Color scheme should follow Mantine theme (use theme.colors for mode vs event distinction)
- Mode indicator: Use color like `blue` or `yellow` for clarification mode to clearly indicate active state
- Event feedback: Use more subtle colors like `gray` with optional spinner/loader for transient status
- Make component extensible for future modes/events (use union types for type safety)
- Handle text truncation gracefully with CSS or Mantine Text component truncation
- Support responsive layouts
- Consider using Mantine Badge or Pill component for mode indication
- For event feedback, consider using Loader component for animated states

**Event Notification Implementation**:

- Use `useState` to track current event notification state (message, visibility)
- Use `useEffect` with cleanup to handle auto-hide timeout when `show()` is called with duration
- Use `useImperativeHandle` if using forwardRef, or expose via context/hook
- When `show(message, durationSeconds)` is called:
  - Set event state with message
  - Clear any existing timeout
  - If durationSeconds provided, set timeout to call `hide()` after duration * 1000ms
- When `hide()` is called:
  - Clear event state (set to undefined/null)
  - Clear any pending timeout
- Component should render event feedback only when event state is present

## Testing Considerations

- Verify clarification mode activates automatically when clarification message arrives via SSE
- Verify clarification mode activates on initial load if last message requires clarification
- Verify StatusIndicator appears/disappears correctly with mode props
- Verify StatusIndicator displays correct initial question text
- Verify StatusIndicator uses appropriate color styling for mode indication
- Verify reasoning-id is included in message content when clarification mode is active
- Verify reasoning-id is NOT included when clarification mode is cancelled
- Verify single Reply button activates clarification mode without changing input value
- Verify cancel button in StatusIndicator clears clarification context
- Verify StatusIndicator component is reusable and extensible for future modes
- Verify StatusIndicator can display event feedback independently (when implemented)
- Verify StatusIndicator can display mode and event feedback simultaneously
- Verify `show(message, durationSeconds?)` displays notification correctly
- Verify auto-hide works after specified duration
- Verify `hide()` immediately hides notification
- Verify timeout is cleared when `hide()` is called manually
- Verify timeout is cleared when new `show()` is called before previous timeout completes
- Test with multiple clarification requests in sequence
