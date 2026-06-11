# WI-294 — Expand navigation + QueryDataView design

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `📐 docs` |
| **Area** | `ui` |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md), [**WI-293**](WI-293-ai-sql-view-condensed-verification.md) |
| **Enables** | [**WI-295**](WI-295-ai-sql-view-query-data-view.md), [**WI-296**](WI-296-ai-sql-view-expand-implementation.md) |

## Goal

Document **expand-view** navigation, **chat-native** expand styling, artefact expand registry, and
**`QueryDataView`** contract (Analysis / condensed / expanded modes).

## Deliver

### Expand navigation

- **Trigger:** Expand when `chatArtifactTreatments[chatType][kind].transitions` includes `expand`.
- **Effect:** full **chat content pane** (message list hidden).
- **Return:** back arrow → originating message (`messageId`, `scrollIntoView`).
- **State:** `ChatExpandState { messageId, turnId, artefactKey, providerKind, returnScrollY? }`.

### Expand registry

```typescript
interface ArtifactExpandProvider {
  kind: string;
  canExpand(group: ArtifactRenderGroup, chatType: ChatType): boolean;
  renderExpanded(props: ArtifactExpandProps): ReactNode;
}
```

- v1: `sql-data-composite` on **`general`** only.

### QueryDataView modes

| Mode | Context | Paging | Toolbar |
|------|---------|--------|---------|
| `condensed` | In-chat Data tab | first page / lazy | minimal, chat density |
| `expanded` | Expand pane | full | chat density + export |
| `playground` | Analysis | full | Analysis chrome |

### Design doc updates

- Extend [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) § expand + `QueryDataView`.

## Acceptance criteria

- [ ] Expand gated by chat type documented.
- [ ] Back-to-message navigation specified.
- [ ] Chat-native vs Analysis styling distinguished.
