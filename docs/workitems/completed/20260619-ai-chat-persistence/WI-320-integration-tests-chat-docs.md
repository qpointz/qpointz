# WI-320 — Integration tests and chat design docs

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [STORY.md](STORY.md) — seq **5** |
| **Depends on** | **WI-317, WI-318, WI-319** |
| **Commit** | `[docs] WI-320: chat persistence IT and design docs` |

### Verify when done

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :ai:mill-ai-autoconfigure:testIT
./gradlew :ai:mill-ai-service:testIT
```

Run [manual checklist](#manual-verification-checklist) on local mill-service + Postgres + mill-ui if available.

### Primary files to touch

| Area | Path |
|------|------|
| Flyway IT | `persistence/mill-persistence/src/testIT/.../FlywayMigrationIT.kt` |
| Autoconfigure IT | `ai/mill-ai-autoconfigure/src/testIT/.../AiV3JpaAutoConfigurationIT.kt` |
| Service IT | `ai/mill-ai-service/src/testIT/.../AiChatControllerIT.kt` |
| Design | `docs/design/agentic/v3-chat-service.md` |
| Design | `docs/design/agentic/v3-conversation-persistence.md` |
| Design | `docs/design/agentic/artifact-foundation.md` (§5 POC + `persist`) |
| Trackers | `docs/workitems/BACKLOG.md` — A-75 → `done` |

### Implementation checklist

1. Extend ITs: restart survival, profile-switch turns, multi-user isolation, artifact counts.
2. Update design docs for `ai_chat`, ownership, ephemeral artifacts.
3. Run manual checklist; note results in WI or MR.
4. Mark `[x]` in [STORY.md](STORY.md).

---

## Goal

Regression coverage for WI-317–319 and design doc updates for unified chat persistence, per-turn profile history, and security wiring. Story-level manual verification checklist.

**Depends on WI-317, WI-318, WI-319.**

## Scope

### Tests

- Consolidate / extend autoconfigure IT (`JpaChatRegistry` → `ai_chat`) from WI-317.
- Service IT: restart-survival; **`ai_chat` row before first message**.
- **Profile-switch IT:** profile A → send → PATCH profile B → send → GET chat → each turn's `profileId` matches exchange (from WI-317).
- Multi-user isolation from WI-318.
- Artifact count assertions after SQL turn (WI-319) in unit or IT.
- Update [`FlywayMigrationIT`](../../../../persistence/mill-persistence/src/testIT/kotlin/io/qpointz/mill/persistence/FlywayMigrationIT.kt) expected table names: `ai_chat`, `ai_chat_turn` (not `ai_conversation`, `ai_chat_metadata`).

### Design docs

- Update [`docs/design/agentic/v3-chat-service.md`](../../../design/agentic/v3-chat-service.md): unified **`ai_chat` / `ai_chat_turn`**, per-turn **`profile_id`**, JPA wiring, `SecurityUserIdResolver`, ownership.
- Update [`docs/design/agentic/v3-conversation-persistence.md`](../../../design/agentic/v3-conversation-persistence.md): rename narrative to `ai_chat` / `chat_id`; document header vs turn `profile_id`; drop split metadata/conversation table story.
- Update [`docs/design/agentic/artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) §5 POC table with `persist` flag and ephemeral kinds.

### Trackers (at story closure or this WI if last)

- BACKLOG **A-75** → `done` (ownership; primary delivery WI-318).
- Note **A-76/A-77** (delete cascade) remain backlog.

## Out of scope

- Capability YAML schema reference — **WI-321**.
- Chat hard-delete cascade implementation.
- mill-ui per-message profile badges (optional follow-up).

## Manual verification checklist

1. Enable security + AI profile; login via mill-ui.
2. Create general chat with profile A; send message.
3. Switch profile to B in toolbar; send second message.
4. Optional Run on SQL artefact.
5. Restart mill-service (same DB).
6. Chat in sidebar; open restores transcript + SQL/data artefacts.
7. Inspect GET response or DB: earlier turns show profile A, later turns profile B (`TurnResponse.profileId` / `ai_chat_turn.profile_id`).
8. DB: no new `sql.validation` rows; **`ai_chat.user_id`** matches logged-in user.

## Acceptance

- `./gradlew :ai:mill-ai-persistence:testIT :ai:mill-ai-autoconfigure:testIT :ai:mill-ai-service:testIT` pass.
- Design docs reflect implemented behavior.
- Manual checklist verified on integration or local mill-service.

## Modules

- `persistence/mill-persistence` (testIT)
- `ai/mill-ai-autoconfigure` (testIT)
- `ai/mill-ai-service` (testIT)
- `docs/design/agentic/`
