# WI-318 — Auth-bound user and ownership checks

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [STORY.md](STORY.md) — seq **3** |
| **Depends on** | **WI-317** (`ai_chat.user_id`, JPA `ChatRegistry`) |
| **Backlog** | Closes **A-75** |
| **Commit** | `[feat] WI-318: SecurityUserIdResolver and chat ownership` |

### Verify when done

```bash
./gradlew :ai:mill-ai-service:test --tests "*UnifiedChatService*"
./gradlew :ai:mill-ai-service:testIT
```

### Primary files to touch

| Area | Path |
|------|------|
| New resolver | `security/mill-security-auth-service` or `security/mill-service-security` — `SecurityUserIdResolver` |
| Autoconfigure | `ai/mill-ai-autoconfigure/.../AiV3AutoConfiguration.kt` — bean ordering / `@ConditionalOnMissingBean` |
| Enforcement | `ai/mill-ai-service/.../UnifiedChatService.kt` — `requireOwned()` on get/update/delete/send/attach/profile PATCH |
| Config ref | `apps/mill-service/src/main/resources/application.yml` — `mill.ai.chat.default-user-id`, `mill.security.enable` |
| Auth pattern | `security/.../AuthController.kt` — `getMe` identity resolution |

### Implementation checklist

1. Implement `SecurityUserIdResolver` → `ResolvedUser.userId` when security on; else `PropertiesUserIdResolver`.
2. Register bean when auth modules present.
3. Add `requireOwned(metadata)`; 404 on cross-user access (no leak).
4. Unit + IT tests (two users, list isolation).
5. Mark `[x]` in [STORY.md](STORY.md).

---

## Goal

Bind chat create/list/access to the authenticated user's canonical `userId` (stable UUID from `ResolvedUser`), replacing the static `mill.ai.chat.default-user-id` for secured deployments. Enforce ownership on all operations that load by `chatId`.

Closes BACKLOG **A-75**.

**Depends on WI-317** — ownership column lives on unified **`ai_chat.user_id`**.

## Context

- **`ai_chat.user_id`** (WI-317) and [`ChatResponse.userId`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) model ownership — no new DB column.
- [`UnifiedChatService.listChats()`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt) filters by `userIdResolver.resolve()` but resolver is always `PropertiesUserIdResolver("default")` unless overridden.
- `getChat`, `updateChat`, `deleteChat`, `sendMessage`, `attachExecutionResult` do not verify owner ([`v3-implementation-findings.md`](../../../design/agentic/v3-implementation-findings.md)).
- [`JpaChatRegistry`](../../../../ai/mill-ai-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaChatRegistry.kt) (WI-317) must be wired so list/create persist `user_id` on **`ai_chat`**.

## Scope

1. **`SecurityUserIdResolver`** (new bean, e.g. in `mill-security-auth-service` or `mill-service-security`):
   - When `mill.security.enable=true`: resolve `(provider, subject)` from `SecurityContext` (same as [`AuthController.getMe`](../../../../security/mill-security-auth-service/src/main/kotlin/io/qpointz/mill/security/auth/controllers/AuthController.kt)) → `UserIdentityResolutionService` → `ResolvedUser.userId`.
   - When security disabled: delegate to `PropertiesUserIdResolver(props.defaultUserId)`.
   - Register `@ConditionalOnMissingBean(UserIdResolver::class)` when auth modules present; keep properties fallback in `AiV3AutoConfiguration`.

2. **Ownership enforcement** in `UnifiedChatService`:
   - `requireOwned(metadata)` before read/mutate/send on `chatId`.
   - Return `null` / 404 when `metadata.userId != userIdResolver.resolve()` (no existence leak).
   - Apply to `updateChatProfile` (profile switch) as well as get/update/delete/send/attach.

3. **Tests**:
   - Unit: user A cannot read/update/delete/send on user B's chat.
   - IT: two user ids → list isolation; cross-user get returns 404.

## Out of scope

- Exposing login username as `createdBy` display field (use `userId`; profile join is follow-up).
- UI changes (mill-ui already sends session cookie on chat API calls).
- Per-turn `profile_id` (WI-317).

## Acceptance

- Login as user A → create chat → **`ai_chat.user_id`** = A's UUID.
- `GET /api/v1/ai/chats` returns only A's general chats.
- User B with A's `chatId` gets 404 on get/update/delete/send/profile PATCH.
- Security disabled: behavior unchanged (`default-user-id`).
- BACKLOG **A-75** marked `done` at story closure.

## Modules

- `security/mill-security-auth-service` or `security/mill-service-security`
- `ai/mill-ai-autoconfigure`
- `ai/mill-ai-service`
