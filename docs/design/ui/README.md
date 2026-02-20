# Frontend and UX

Design documents for the Mill Grinder UI and general user experience patterns.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Grinder UI visual design, layout, dark mode, look-and-feel
- Chat input component design (command palette, @-mentions, enhancements)
- Clarification and notification UX flows (purely UI-pattern focused)
- Interaction patterns, component architecture, or frontend refactoring

## Does NOT Belong Here

- Metadata UI implementation that is primarily about the metadata domain → `metadata/`
- AI UX specification (how the AI agent talks to users) → `ai/`
- Backend API design that happens to have a UI consumer → respective domain folder

## Classification Note

Apply the "primary domain" principle from the root README. A document about "metadata UI"
goes in `metadata/` because its primary concern is the metadata domain. Only documents
that are purely about UI patterns, components, or visual design with no domain-specific
content belong here.

## Documents

| File | Description |
|------|-------------|
| `chat-input-enhancements.md` | Command palette (`/`) and @-mentions for intents and metadata in chat input |
| `mill-grinder-ui-refresh.md` | Mill Grinder UI visual refresh: look-and-feel, consistency, dark mode |
| `ux-clarification-and-notification.md` | Clarification UI flow refactor: auto-activate, single-line indicator, reply button |
