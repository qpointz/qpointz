# WI-294 — Condensed-path verification

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `🧪 test` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-290**](WI-290-ai-sql-view-get-artifact-replay.md)–[**WI-293**](WI-293-ai-sql-view-chat-surfaces-parity.md) |
| **Enables** | [**WI-295**](WI-295-ai-sql-view-expand-design.md) |

## Goal

Verify condensed preview, chat-type wiring, and backend replay wire **before** expand (WI-295–298).
Confirm artefacts foundation still green (no regression from service-layer additions).

## Deliver

### Automated — artefacts foundation (regression gate)

```bash
./gradlew :ai:mill-ai-test:testIT --tests "*ArtifactEmit*"
./gradlew :ai:mill-ai:test --tests "*ArtifactEmission*"
```

### Automated — sql-view scope

```bash
./gradlew :ai:mill-ai-service:test :ai:mill-ai-persistence:testIT
cd ui/mill-ui && npm run test && npm run build
```

### Manual smoke — `general` chat

- [ ] Agent emits structured SQL artefact (not JSON/prose in bubble).
- [ ] Live SQL → Run → Data; switch SQL ↔ Data tabs.
- [ ] Export when results exist.
- [ ] Open in Analysis: `chatHandoff` (sql, suggestedName, suggestedDescription); no executionId.
- [ ] Lazy hydrate on scroll + Data tab; no fetch on chat open.
- [ ] ~900px layout; chat-native card styling.

### Manual smoke — chat types

- [ ] `inline-analysis`: SQL host-applied; no preview card.
- [ ] `inline-model` / `inline-knowledge`: facet/schema cards via `ArtifactCard`.

## Acceptance criteria

- [ ] Artefacts scenario packs still green.
- [ ] Scoped sql-view tests green for WI-290–293.
- [ ] No salvage code in codebase.
- [ ] [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) covers condensed path.
- [ ] No story archive — closure is [**WI-299**](WI-299-ai-sql-view-verification-closure.md).
