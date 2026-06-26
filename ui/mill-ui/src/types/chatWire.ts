/**
 * Wire shapes for unified AI v3 chat HTTP endpoints.
 *
 * Mirrors Kotlin DTOs in `ai/mill-ai-service/.../dto/ChatDtos.kt` and
 * `AgentProfileResponse` so TS can be diff-checked against backend JSON.
 */

/** Kotlin: ChatResponse — list/detail/create/update payloads. */
export interface ChatResponseWire {
  chatId: string;
  userId: string;
  profileId: string;
  chatName: string;
  chatType: string;
  isFavorite: boolean;
  contextType: string | null;
  contextId: string | null;
  contextLabel: string | null;
  contextEntityType: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Kotlin: TurnResponse — durable transcript rows. */
export interface TurnResponseWire {
  turnId: string;
  role: string;
  text: string | null;
  createdAt: string;
  artifacts?: ArtifactResponseWire[];
  /** Mill-ui assistant layout hint; null until persistence stores it. */
  assistantReplyView?: string | null;
}

/** Kotlin: ArtifactResponse — durable structured turn artifacts. */
export interface ArtifactResponseWire {
  kind: string;
  payload: Record<string, unknown>;
  artifactId?: string | null;
  urn?: string | null;
  status?: string | null;
}

/** Kotlin: ChatDetailResponse — GET `/api/v1/ai/chats/{chatId}`. */
export interface ChatDetailResponseWire {
  chat: ChatResponseWire;
  messages: TurnResponseWire[];
}

/** Kotlin: CreateChatHttpRequest — POST `/api/v1/ai/chats` body fields. */
export interface CreateChatRequestWire {
  profileId?: string | null;
  contextType?: string | null;
  contextId?: string | null;
  contextLabel?: string | null;
  contextEntityType?: string | null;
}

/** Kotlin: UpdateChatHttpRequest — PATCH `/api/v1/ai/chats/{chatId}` body fields. */
export interface UpdateChatRequestWire {
  chatName?: string | null;
  isFavorite?: boolean | null;
  contextLabel?: string | null;
  profileId?: string | null;
}

/** Kotlin: SendMessageHttpRequest — `{ "message": "..." }` for SSE POST. */
export interface SendMessageRequestWire {
  message: string;
}

/** Kotlin: AgentProfileResponse — GET `/api/v1/ai/profiles`. */
export interface AgentProfileResponseWire {
  id: string;
  capabilityIds: string[];
}
