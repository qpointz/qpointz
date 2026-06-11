# WI-299 — Story verification and closure

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `🧪 test` / `📐 docs` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-298**](WI-298-ai-sql-view-expand-sql-wiring.md) |
| **Enables** | Story archive |

## Goal

End-to-end verification (condensed + expand + chat types), update tracking docs, archive story.
Confirm clean restart: artefacts foundation intact, no salvage code.

## Deliver

### Automated — full stack

```bash
./gradlew :ai:mill-ai-test:testIT --tests "*ArtifactEmit*"
./gradlew :ai:mill-ai:test --tests "*ArtifactEmission*"
./gradlew :ai:mill-ai-service:test :ai:mill-ai-persistence:testIT
cd ui/mill-ui && npm run test && npm run build
```

### Manual smoke — full story

- [ ] **General:** agent emits structured SQL → condensed → Run → Data → Export → Expand → paging → Back.
- [ ] Open in Analysis from condensed and expand (SQL-only; no executionId).
- [ ] Chat-native styling in condensed and expand.
- [ ] **Inline analysis:** host-apply only; no preview/expand.
- [ ] Facet/schema/unknown via artefacts-branch `ArtifactCard`.
- [ ] Lazy history hydrate; `chatSqlExecute` flag behaviour.
- [ ] Analysis playground OK after `QueryDataView` refactor.
- [ ] **No** JSON/prose SQL in message bubble when agent follows emit contract.

### Design documentation

- [ ] [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) complete; § emission cross-links artefacts story.
- [ ] Extension cookbook validated.
- [ ] [`docs/design/ai/README.md`](../../../design/ai/README.md) indexed.

### Story closure

- [ ] Update [`BACKLOG.md`](../../BACKLOG.md) and [`MILESTONE.md`](../../MILESTONE.md).
- [ ] Archive to `docs/workitems/completed/YYYYMMDD-ai-sql-view-restart/`.
- [ ] Note abandonment of `feat/ai-chat-sql-result-view` in closure commit message.
- [ ] MR-ready commits per [`RULES.md`](../../RULES.md).

## Closure checklist extras

- [ ] [`RESTART-NOTES.md`](RESTART-NOTES.md) still accurate vs final implementation.
- [ ] No files from do-not-port list present in diff vs `feat/ai-chat-artefacts` base.
- [ ] Abandoned `in-progress/ai-sql-view/` not updated (this story supersedes it).

## Acceptance criteria

- [ ] STORY.md all WIs `[x]`.
- [ ] `QueryDataView` in playground, condensed, expanded modes.
- [ ] No salvage code (`GeneratedSqlAnswerSalvage`, client prose inference).
- [ ] Artefacts scenario packs still green.
- [ ] Single story delivers presentation layer on artefacts foundation.
