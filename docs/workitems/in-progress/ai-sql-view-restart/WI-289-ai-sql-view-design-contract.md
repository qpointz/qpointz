# WI-289 — Design contract and wire model

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `📐 design` |
| **Area** | `ai`, `ui` |
| **Depends on** | Artefacts prerequisite gate ([`STORY.md`](STORY.md) § Prerequisite) |
| **Enables** | [**WI-290**](WI-290-ai-sql-view-backend-replay-attach.md), [**WI-291**](WI-291-ai-sql-view-preview-framework.md) |

## Goal

Lock the **chat-inferred artefact presentation** contract before implementation: wire shapes,
**context-aware** views/transitions (SQL first), client-only execution, lazy replay, and expand design
for **WI-294–296**. The framework must generalize to charts, metadata, data-quality rules,
etc. — not every kind uses every view or transition.

**Emission foundation is out of scope** — owned by [`ai-artifact-emit-contract`](../../in-progress/ai-artifact-emit-contract/STORY.md)
and documented in [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md). This WI
documents **presentation + replay layers only** and cross-links emission (does not reimplement or
duplicate coordinator/registry/SSE content).

## Deliver

### Chat artefact presentation framework (standard pattern)

**Core rule:** each **chat type** defines how **inferred artefacts** are treated on arrival — preview,
host-apply, card, expand, navigate, or ignore. Artefact kinds (SQL, chart, metadata, DQ rules, …) do
not own global UX; the **active chat type** selects the treatment.

Document in [`ai-v3-chat-transport-extensions.md`](../../../design/agentic/ai-v3-chat-transport-extensions.md)
and [`GENERAL-CHAT-DESIGN.md`](../../../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md).

**Chat types** (`ChatType`) — first key in the registry:

| ChatType | Host UI | Artefact treatment intent |
|----------|---------|---------------------------|
| `general` | `/chat` full-page | Rich previews, expand, open-in-analysis |
| `inline-analysis` | Analysis drawer + editor | **Host-apply SQL**; minimal in-drawer chrome |
| `inline-model` | Model explorer drawer | Facet/metadata cards; compact SQL preview (future) |
| `inline-knowledge` | Knowledge drawer | Concept/constraint cards |
| `context-bound` (extensible) | Embedded panel per context | Per-context profile (register new chat types) |

```typescript
/** Each chat type registers artefact treatments — chat type is primary, kind is secondary. */
type ChatArtifactTreatmentRegistry = Record<
  ChatType,
  Partial<Record<ArtefactKind, ArtifactTreatment>>
>;

interface ArtifactTreatment {
  mode: ArtifactPresentationMode;
  views?: ('condensed' | 'expanded')[];
  transitions?: ArtifactTransition[];
  actions?: ArtifactActionId[];
}
```

Lookup: `registry[chatType][artefactKind]` → treatment; missing entry → safe default for that chat type
(e.g. `prose-only` or `conversation-card`).

**Presentation modes** — not every chat type uses every mode for every kind:

| Mode | Meaning |
|------|---------|
| `condensed-preview` | In-message card (~900px), optional tabs, bounded height |
| `expand` | Drill-down to full chat content pane (WI-294–296) |
| `host-apply` | **No preview card** — apply payload to parent host (e.g. SQL → editor) |
| `conversation-card` | Full-width knowledge/metadata card in thread |
| `prose-only` | Artefact absorbed into text bubble; no structured chrome |

**v1 treatment matrix (by chat type → kind; others stubbed):**

| ChatType → Kind | Treatment |
|-----------------|-----------|
| `general` → `sql-data-composite` | `condensed-preview` + Run/Export/Expand/Open-in-Analysis |
| `inline-analysis` → `sql-data-composite` | **`host-apply`** — SQL to editor; no preview/Run in drawer |
| `inline-model` / `inline-knowledge` → `sql-data-composite` | `condensed-preview` compact (future) |
| `general` → `facet-proposal` | `conversation-card` via existing [`ArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/ArtifactCard.tsx) |
| `*` → `schema-capture` / `unknown` | Existing artefacts-branch cards — **do not remove** |

### Extend transport and UI design docs

- Client `data` artefact / GET wire `kind: data` JSON (`executionId`, columns, `rowCount`, `truncated`, sql ref).
- **Chat artefact treatment framework** (above): `ChatType`, modes, transitions, `chatArtifactTreatments`.
- **Extension guide:** kind registry, grouping (`sql-data-composite`), per-**chat-type** treatments.
- **In-chat actions:** Run (client `queryService`), Copy, Export, Open in Analysis, Expand (WI-296).
- **Live vs replay:** eager `executeQuery` on Run; lazy `fetchQueryPage` on viewport + Data tab focus.
- Explicitly **no** AI-chat `execute-sql` endpoint.

### Chat-native styling (condensed + expand)

Both **in-chat** and **expand** views share one visual system — continuous with assistant messages.
Evolve [`SqlArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/SqlArtifactCard.tsx) styling
into `ChatArtifactCard` / `SqlDataCondensedPreview` for **general** SQL — do not replace facet/schema/unknown cards from artefacts branch.

### API sketch (for WI-290)

- `GET /api/v1/ai/chats/{id}` → `TurnResponse.artifacts[]`, `assistantReplyView` (`sql-primary`, …).
- `POST /api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result` — attach metadata only
  (`executionId`, columns, `rowCount`); **no** server SQL execution.

### Open in Analysis (in-chat + expand)

Transient `chatHandoff` router state — SQL only, no `executionId`, no auto-save.

### Primary design doc — [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md)

Sections 1–11 as in original plan, with these restart additions:

- **§ Emission** — cross-link [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md);
  state that coordinator/registry/SSE are **not** implemented in this story.
- **§ Presentation** — condensed, expand, treatments (this story).
- **§ Replay wire** — `ArtifactWireMapper`, attach-result (WI-290).
- Story path references **`ai-sql-view-restart`** (not abandoned `in-progress/ai-sql-view`).

### Do not port (from abandoned `feat/ai-chat-sql-result-view`)

| Item | Reason |
|------|--------|
| `GeneratedSqlAnswerSalvage` | Superseded by `ArtifactEmissionCoordinator` |
| Inline `AgentEventRouter` body changes | Use `RegistryAgentEventRouter` from artefacts branch |
| `LangChain4jAgent` emission/salvage edits | Artefacts branch owns runtime |
| Client prose/JSON salvage (`inferSqlArtifactFromProse`, `resolveMessageArtifacts`, `assistantDisplayContent`) | Symptom workarounds |
| Deletion of `AssistantReplyRouter`, `SchemaCaptureArtifactCard`, `UnknownArtifactCard` | Extend, do not delete |
| Blind `git checkout` of old-branch integration files | Fresh rewrite against foundation APIs |

**Light reference only:** old branch additive paths (`artifactPreview/`, `expand/`, `data/`, `ArtifactWireMapper`) for UX and layout ideas.

See [`RESTART-NOTES.md`](RESTART-NOTES.md) for full porting catalogue, conflict file list, and red-flag review checklist.

## Acceptance criteria

- [ ] Design docs updated; emission cross-linked to [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) (not reimplemented here).
- [ ] `chatArtifactTreatments` matrix documented for v1 chat types + SQL/facet/schema/unknown.
- [ ] Open in Analysis handoff documented (no save, no executionId).
- [ ] [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) drafted with sections 1–11 + emission cross-link.
- [ ] Do-not-port list documented in STORY.md and this WI.
- [ ] No implementation code in this WI (docs only).
