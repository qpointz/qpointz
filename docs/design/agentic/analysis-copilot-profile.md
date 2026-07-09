# Analysis copilot backend profile

Agent profile for Analysis inline chat (`analysis-copilot`). Complements
[`INLINE-CHAT-FOUNDATION.md`](../ui/mill-ui/INLINE-CHAT-FOUNDATION.md).

## Profile

| Field | Value |
|-------|-------|
| id | `analysis-copilot` |
| YAML | `ai/mill-ai/src/main/resources/profiles/platform-agent-profiles.yaml` |
| Capabilities | `conversation`, `schema`, `metadata`, `metadata-authoring`, `sql-dialect`, `sql-query` |
| Excluded | `chart-mapping`, `concept`, `value-mapping` |

Capability providers that participate in Analysis contextual chats declare
`supportedContexts` including `"analysis"`.

## Turn context

`POST /api/v1/ai/chats/{chatId}/messages` accepts optional:

```json
{
  "message": "...",
  "context": { "values": { "sql.current": "..." }, "version": 1 }
}
```

Unknown keys are tolerated. Ephemeral only — not stored on durable turns.

For `analysis-copilot`, sanitized excerpts injected into the system prompt:

- `sql.current`
- `artifact.query.name`
- `artifact.query.description`
- `execution.last.error`

Raw result rows are never promoted to prompts.

## Related

- UI host key: `inline-analysis` (not a profile id)
- Story: `docs/workitems/in-progress/analysis-inline-chat-foundation/`
