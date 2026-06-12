# WI-293 — Condensed-path verification (checkpoint)

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `🧪 test` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-290**](WI-290-ai-sql-view-backend-replay-attach.md)–[**WI-292**](WI-292-ai-sql-view-chat-wiring.md) |
| **Enables** | [**WI-294**](WI-294-ai-sql-view-expand-design.md) |

## Goal

Verify condensed preview, chat-type wiring, and backend replay wire **before** expand work (WI-294–296).
Confirm artefacts foundation still green (no regression from service-layer and UI additions).

**Hard checkpoint:** do not start WI-294+ until this WI passes.

## Deliver

### Automated — artefacts foundation (regression gate)

```bash
./gradlew :ai:mill-ai-test:testIT --tests "*ArtifactEmit*"
./gradlew :ai:mill-ai:test --tests "*ArtifactEmission*"
```

### Automated — sql-view scope (WI-290–292)

```bash
./gradlew :ai:mill-ai-service:test :ai:mill-ai-persistence:testIT
cd ui/mill-ui && npm run test && npm run build
```

### Manual smoke — `general` chat (`data-analysis` profile)

- [ ] Agent emits structured SQL artefact (not JSON/prose in bubble).
- [ ] Live SQL → Run → Data; switch SQL ↔ Data tabs.
- [ ] Export when results exist.
- [ ] Open in Analysis: `chatHandoff` (sql, suggestedName, suggestedDescription); no executionId.
- [ ] Lazy hydrate on scroll + Data tab; no fetch on chat open.
- [ ] ~900px layout; chat-native card styling.

### Manual smoke — chat types

- [ ] `inline-analysis`: SQL host-applied; no preview card.
- [ ] `inline-model` / `inline-knowledge`: facet/schema cards via `ArtifactCard`.

### Diff review

- [ ] No files from do-not-port list vs artefacts foundation base ([`STORY.md`](STORY.md), [`RESTART-NOTES.md`](RESTART-NOTES.md)).

## Acceptance criteria

- [x] Artefacts scenario packs still green.
- [x] Scoped sql-view tests green for WI-290–292.
- [x] No salvage code in codebase.
- [x] [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) covers condensed path.
- [x] No story archive — closure is [**WI-297**](WI-297-ai-sql-view-closure.md).
