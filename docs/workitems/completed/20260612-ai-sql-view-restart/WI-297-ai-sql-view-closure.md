# WI-297 — Story verification and closure

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `🧪 test` / `📐 docs` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-298**](WI-298-chat-profile-switch.md) |
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

- [x] **General:** agent emits structured SQL → condensed → Run → Data → Export → Expand → paging → Back.
- [x] Open in Analysis from condensed and expand (SQL-only; no executionId).
- [x] Chat-native styling in condensed and expand.
- [x] **Inline analysis:** host-apply only; no preview/expand.
- [x] Facet/schema/unknown via artefacts-branch `ArtifactCard`.
- [x] Lazy history hydrate; `chatSqlExecute` flag behaviour.
- [x] Analysis playground OK after `QueryDataView` refactor.
- [x] **No** JSON/prose SQL in message bubble when agent follows emit contract.

### Design documentation

- [x] [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) complete; § emission cross-links [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md).
- [x] Extension cookbook validated.
- [x] [`docs/design/ai/README.md`](../../../design/ai/README.md) indexed.

### Story closure

- [x] Update [`BACKLOG.md`](../../BACKLOG.md) and [`MILESTONE.md`](../../MILESTONE.md).
- [x] Archive to `docs/workitems/completed/20260612-ai-sql-view-restart/`.
- [x] Note abandonment of `feat/ai-chat-sql-result-view` in closure commit message.
- [ ] MR-ready commits per [`RULES.md`](../../RULES.md) (~6–10 logical commits above merge base) — follow-up before merge.
- [ ] If history was squashed and branch was already pushed: `git push --force-with-lease origin feat/ai-sql-view-2-restart`.

### Per-WI delivery (WI-289–296 — not closure)

Each non-closure WI must end with: tracker `[x]` in [`STORY.md`](STORY.md) → one commit → `git push origin HEAD`.
See [`STORY.md`](STORY.md) § Branching, tracker, commit, and push.

## Closure checklist extras

- [x] [`RESTART-NOTES.md`](RESTART-NOTES.md) accurate vs final implementation.
- [x] No files from do-not-port list present in diff vs artefacts foundation base.
- [x] Abandoned `in-progress/ai-sql-view/` not updated (this story supersedes it).

## Acceptance criteria

- [x] STORY.md all WIs `[x]`.
- [x] `QueryDataView` in playground, condensed, expanded modes.
- [x] No salvage code (`GeneratedSqlAnswerSalvage`, client prose inference, `resolveMessageArtifacts`).
- [x] Artefacts scenario packs still green (verified on branch; run `*ArtifactEmit*` before merge if rebased).
- [x] Single story delivers presentation layer on artefacts foundation.
