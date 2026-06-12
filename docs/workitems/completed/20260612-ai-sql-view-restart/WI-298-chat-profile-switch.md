# WI-298 — Mid-chat agent profile switch

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-296**](WI-296-ai-sql-view-expand-implementation.md) (chat toolbar shell) |
| **Enables** | [**WI-297**](WI-297-ai-sql-view-closure.md) |

## Goal

Allow users to **change the agent profile** on an existing **general** chat from the chat content
toolbar, without creating a new conversation. The next streamed turn must use the newly selected
profile (capabilities, tools, prompts). **LLM memory is retained** across the switch — prior turns
remain in the sliding window for the next send.

Today [`ChatUpdate`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ChatRegistry.kt)
and [`UpdateChatHttpRequest`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt)
omit `profileId`; the toolbar shows a read-only badge ([`ChatToolbar.tsx`](../../../../ui/mill-ui/src/components/chat/ChatToolbar.tsx)).

Runtime already rehydrates from `ChatMetadata.profileId` on every send
([`LangChain4jChatRuntime.send`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt)),
so persisting an updated `profileId` is sufficient for future messages.

## Scope

| In | Out |
|----|-----|
| `PATCH /api/v1/ai/chats/{chatId}` accepts optional `profileId` | Contextual / inline chats (profile fixed at create) |
| Validation against [`ProfileRegistry`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/ProfileRegistry.kt) | Clearing LLM memory on switch |
| Sync denormalized `ConversationEntity.profileId` when conversation exists | Per-turn profile metadata on historical turns |
| Chat toolbar `Select` when ≥2 profiles advertised | Sidebar create-time picker behaviour (`chatAgentPicker`) |
| Unit + integration tests (registry, service, UI service) | Dynamic profile store / admin UI |

## Deliver

### Backend — contract

- [`ChatUpdate`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ChatRegistry.kt): add `profileId: String? = null`; update KDoc (mutable for general chats; enforced in service).
- [`UpdateChatHttpRequest`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt): add `profileId`; map in `toChatUpdate()`.
- [`InMemoryChatRegistry`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/InMemoryChatRegistry.kt) + [`JpaChatRegistry`](../../../../ai/mill-ai-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaChatRegistry.kt): apply `profileId` when non-null.

### Backend — conversation sync

- [`ConversationStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ConversationStore.kt): add `updateProfileId(conversationId, profileId)` (default no-op).
- Implement in [`InMemoryConversationStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/InMemoryConversationStore.kt) and [`JpaConversationStore`](../../../../ai/mill-ai-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaConversationStore.kt).

### Backend — orchestration

- [`UnifiedChatService`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt):
  - Inject `ProfileRegistry` (wire in [`AiV3ChatServiceAutoConfiguration`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3ChatServiceAutoConfiguration.kt)).
  - On `updateChat` with `profileId`:
    - Reject unknown profile → 400.
    - Reject when `chatType != "general"` → 400.
    - No-op when same id.
    - Update registry; call `conversationStore.updateProfileId` when conversation row exists.
    - **Do not** call `chatMemoryStore.clear`.
- [`AiChatController`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt): map validation failures to `MillStatuses.badRequest`.

### Backend — tests

- [`InMemoryChatRegistryTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/InMemoryChatRegistryTest.kt): profile update.
- [`JpaChatRegistryIT`](../../../../ai/mill-ai-persistence/src/testIT/kotlin/io/qpointz/mill/persistence/ai/jpa/JpaChatRegistryIT.kt): profile update.
- [`UnifiedChatServiceTest`](../../../../ai/mill-ai-service/src/test/kotlin/io/qpointz/mill/ai/service/UnifiedChatServiceTest.kt): happy path, unknown profile, contextual rejection, conversation sync.

### Frontend

- [`UpdateChatRequestWire`](../../../../ui/mill-ui/src/types/chatWire.ts): add `profileId?: string | null`.
- [`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts): `updateChatProfile(chatId, profileId)` → PATCH; mock parity.
- [`ChatContext.tsx`](../../../../ui/mill-ui/src/context/ChatContext.tsx): `UPDATE_CONVERSATION_PROFILE` reducer + `updateConversationProfile` callback.
- [`ChatToolbar.tsx`](../../../../ui/mill-ui/src/components/chat/ChatToolbar.tsx): Mantine `Select` when `agentProfiles.length >= 2`; disabled while streaming or run-all in progress; single profile → read-only badge.
- Wire from [`ChatArea.tsx`](../../../../ui/mill-ui/src/components/chat/ChatArea.tsx).
- Test in [`chatService.test.ts`](../../../../ui/mill-ui/src/services/__tests__/chatService.test.ts).

### Documentation

- [`GENERAL-CHAT-DESIGN.md`](../../../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md): mid-chat profile switch, memory retention, general-chat-only rule.
- Cross-link in [`v3-chat-service.md`](../../../design/agentic/v3-chat-service.md) § chat update.

## Behaviour notes

- **Memory retention:** switching profile does not clear [`ChatMemoryStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/memory/ChatMemoryStore.kt). The next turn may include tool/results phrasing from the previous profile in LLM context — accepted for v1.
- **Historical turns:** durable transcript and artefacts are unchanged; only subsequent agent runs use the new profile.
- **Contextual chats:** `PATCH` with `profileId` returns 400; inline hosts unchanged.

## Verification

```bash
./gradlew :ai:mill-ai:test :ai:mill-ai-service:test :ai:mill-ai-persistence:testIT
cd ui/mill-ui && npm run test -- chatService
```

Manual:

- [ ] General chat: toolbar Select lists profiles from `GET /api/v1/ai/profiles`; change profile; send message; observe different capability behaviour.
- [ ] Reload chat: toolbar shows persisted profile.
- [ ] Select disabled while assistant is streaming.
- [ ] Contextual chat: PATCH with `profileId` → 400 (API test or manual).

## Acceptance criteria

- [x] `PATCH { "profileId": "<valid>" }` updates general chat metadata; next SSE turn uses new profile.
- [x] Unknown profile → 400; contextual chat profile change → 400.
- [x] Toolbar Select reflects server state; disabled during active stream.
- [x] Tests green per Verification above.
- [x] Design docs updated.
