# Chat artefact architecture

End-to-end design for **chat-inferred artefact presentation** in Mill UI: replay wire → chat-type
treatment → condensed / expand views → host integrations.

**Story:** [`docs/workitems/in-progress/ai-sql-view-restart/`](../../workitems/in-progress/ai-sql-view-restart/STORY.md) (WI-289–297).

## 1. Layer separation

| Layer | Owner | Doc |
|-------|-------|-----|
| **Emission** (coordinator, descriptors, router, live SSE) | `ai-artifact-emit-contract` | [`artifact-foundation.md`](../agentic/artifact-foundation.md) |
| **Replay wire** (GET artefacts, attach-result) | `ai-sql-view-restart` WI-290 | §4 below |
| **Presentation** (treatments, condensed, expand) | `ai-sql-view-restart` WI-291–296 | §5–§9 below |

**Do not reimplement emission in the sql-view story.** If SQL appears as prose, fix the artefacts
foundation — not client salvage.

## 2. Principles

1. **Chat type is primary** — each `ChatType` registers how artefact *kinds* are treated (preview,
   host-apply, card, expand, navigate). Kinds do not own global UX.
2. **Client-side execution** — SQL runs via `queryService.executeQuery`; `POST …/execution-result`
   stores replay metadata only (no server SQL execution).
3. **Lazy data hydration** — condensed/expand views fetch pages through `executionId`; GET chat replay
   restores `sql` + `data` wire artefacts.
4. **Forward-compatible wire** — unknown structured parts use `UnknownArtifactCard`; text-only clients
   ignore unknown `partType` / `presentation` pairs.

## 3. Emission (cross-link — out of scope here)

Structured chat artefacts are emitted by the v3 agentic runtime per capability YAML descriptors.
See **[`artifact-foundation.md`](../agentic/artifact-foundation.md)** for:

- `ArtifactDescriptor`, `ArtifactEmissionCoordinator`, `RegistryAgentEventRouter`
- POC artefacts: `generated-sql`, facet proposal, schema capture
- SSE contract: `item.part.updated` with `presentation: structured`

This document covers **what happens after** emission reaches mill-ui and **GET replay**.

## 4. Replay wire (WI-290)

### GET chat detail

`GET /api/v1/ai/chats/{id}` → `TurnResponse` per turn:

- `artifacts: List<ArtifactResponse>` — consumer-safe wire kinds
- `assistantReplyView` — layout hint when artefacts present

### ArtifactWireMapper (mill-ai-service)

| Persisted kind | Wire `kind` | Notes |
|----------------|-------------|-------|
| `sql.generated` | `sql` | From agent emission |
| `sql.result` | `data` | From client attach |
| Facet kinds | `facet-proposal` | From agent emission |
| `sql.validation` | *(omitted)* | Audit only |

### Attach after Run

```http
POST /api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result
```

Body: `executionId`, `columns`, `rowCount`, `truncated`, `sql`. Persists `sql.result`; does **not**
execute SQL.

### Data wire payload

```json
{
  "kind": "data",
  "payload": {
    "executionId": "…",
    "sql": "SELECT …",
    "rowCount": 42,
    "truncated": false,
    "columns": [{ "name": "c1", "type": "INTEGER" }]
  }
}
```

Client: [`parseWireArtifacts`](../../../ui/mill-ui/src/utils/artifactWireParse.ts).

## 5. Chat types (v1)

| ChatType | Host | SQL/data treatment |
|----------|------|-------------------|
| `general` | `/chat` | Condensed preview, expand pane, Run/Export/Open in Analysis |
| `inline-analysis` | Query Playground drawer | `host-apply` → editor (no in-drawer preview) |
| `inline-model` / `inline-knowledge` | Explorer drawers | Compact SQL stub; facet/schema via `ArtifactCard` |

Registry: [`chatArtifactTreatments.ts`](../../../ui/mill-ui/src/components/chat/artifactPreview/chatArtifactTreatments.ts).

## 6. Live SSE vs GET replay

| Phase | Artifacts source |
|-------|------------------|
| **Live** | `parseChatStructuredPart` on `item.part.updated` → `Message.artifacts` |
| **GET reload** | `parseWireArtifacts(turn.artifacts)` in `turnToMessage` |

SQL arrives **only** via structured SSE or GET wire — never prose inference.

## 7. UI layers

```mermaid
flowchart TB
  subgraph transport [Transport]
    SSE[SSE item.part.updated]
    GET[GET chat turns + artifacts]
  end
  subgraph state [Client state]
    MSG[Message.artifacts]
    VIEW[assistantReplyView]
  end
  subgraph router [Presentation]
    APR[ArtifactPreviewRouter]
    MAC[MessageArtifactComposer]
    TREAT[chatArtifactTreatments]
  end
  subgraph views [Views]
    COND[SqlDataCondensedPreview]
    EXP[ChatExpandHost / SqlDataExpandedView]
    QDV[QueryDataView]
  end
  SSE --> MSG
  GET --> MSG
  MSG --> APR --> MAC --> TREAT
  TREAT --> COND
  TREAT --> EXP
  COND --> QDV
  EXP --> QDV
```

| Module | Role |
|--------|------|
| `ArtifactPreviewRouter` | Layout chrome from `assistantReplyView` |
| `MessageArtifactComposer` | Treatment by `chatType`; host-apply for inline-analysis |
| `SqlDataCondensedPreview` | In-thread SQL ↔ Data tabs, action bar |
| `ArtifactCard` | Facet, schema-capture, unknown (artefacts foundation) |
| `ChatExpandHost` | Full-pane overlay; back scrolls to originating message |
| `QueryDataView` | Shared grid (`condensed` \| `expanded` \| `playground`) |

## 8. Transitions

| Transition | Behaviour |
|------------|-----------|
| **Expand** | `general` + `sql-data-composite` → `ChatExpandHost` |
| **Open in Analysis** | Navigate to `/analysis` with `location.state.chatHandoff` `{ sql, suggestedName?, suggestedDescription? }` — no save, no `executionId`, no auto-run |
| **Host apply** | `inline-analysis` structured SQL → editor via `hostIntegrations` |

## 9. QueryDataView modes (WI-294–295)

| Mode | Context | Paging | Toolbar |
|------|---------|--------|---------|
| `condensed` | In-chat Data tab | first page / lazy | chat density |
| `expanded` | Expand pane | full | chat density + export |
| `playground` | Analysis | full | Analysis chrome |

## 10. Expand state (WI-294–296)

```typescript
interface ChatExpandState {
  messageId: string;
  turnId?: string;
  artefactKey?: string;
  providerKind?: string;
  returnScrollY?: number;
}
```

Expand gated by `chatArtifactTreatments[chatType][kind].transitions` including `expand`. v1:
`sql-data-composite` on **`general`** only.

## 11. Extension guide

1. Add backend wire mapping in `ArtifactWireMapper` (if new persist kind).
2. Extend `ChatMessageArtifact` + `parseChatStructuredPart` + `parseWireArtifacts`.
3. Register grouping in `artifactGroups.ts` if composite.
4. Add row to `chatArtifactTreatments` per affected `ChatType`.
5. Register preview/card in `registry.tsx`; expand in `expandRegistry.ts` when needed.
6. Vitest + optional scenario pack.

For **new emission kinds**, start with [`artifact-foundation.md`](../agentic/artifact-foundation.md) §8.

## Related docs

- [`artifact-foundation.md`](../agentic/artifact-foundation.md) — emission (canonical)
- [`ai-v3-chat-transport-extensions.md`](../agentic/ai-v3-chat-transport-extensions.md) — SSE / replay
- [`GENERAL-CHAT-DESIGN.md`](../ui/mill-ui/GENERAL-CHAT-DESIGN.md) — general chat UX
- [`capabilities_design.md`](capabilities_design.md) §15 — generate-only SQL
