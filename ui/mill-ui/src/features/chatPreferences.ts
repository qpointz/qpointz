/**
 * Shared agent profile preference for chat create calls (general + inline).
 *
 * Order: explicit `selectedId` (UI) → sessionStorage last-used → `VITE_MILL_AI_PROFILE` → {@link DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID}.
 */
export const GENERAL_CHAT_AGENT_PROFILE_SS_KEY = 'mill-ui-general-chat-agent-profile-id';

/** Default agent profile for new chats when nothing else is configured. */
export const DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID = 'data-analysis';

/** Backend profile id for the Analysis copilot inline chat. */
export const ANALYSIS_COPILOT_PROFILE_ID = 'analysis-copilot';

/** Resolves `profileId` for Analysis copilot contextual chat create. */
export function resolveAnalysisCopilotProfileId(): string {
  return ANALYSIS_COPILOT_PROFILE_ID;
}

/** Read the last profile id chosen in General Chat (session-scoped). */
export function readStoredGeneralChatProfileId(): string | null {
  try {
    return sessionStorage.getItem(GENERAL_CHAT_AGENT_PROFILE_SS_KEY);
  } catch {
    return null;
  }
}

/** Persist picker selection for new chats in this browser tab. */
export function writeStoredGeneralChatProfileId(id: string | null): void {
  try {
    if (id == null || id.trim() === '') {
      sessionStorage.removeItem(GENERAL_CHAT_AGENT_PROFILE_SS_KEY);
    } else {
      sessionStorage.setItem(GENERAL_CHAT_AGENT_PROFILE_SS_KEY, id.trim());
    }
  } catch {
    /* ignore quota / private mode */
  }
}

function envProfileFallback(): string | undefined {
  const raw = import.meta.env.VITE_MILL_AI_PROFILE;
  return typeof raw === 'string' && raw.trim() !== '' ? raw.trim() : undefined;
}

/**
 * Resolves `profileId` for `POST /api/v1/ai/chats`.
 *
 * @param options.selectedId profile chosen in General Chat UI (if any)
 */
export function resolveGeneralChatAgentProfileId(options?: { selectedId?: string | null }): string {
  const fromPicker = options?.selectedId?.trim();
  if (fromPicker) {
    return fromPicker;
  }
  const fromSession = readStoredGeneralChatProfileId()?.trim();
  if (fromSession) {
    return fromSession;
  }
  return envProfileFallback() ?? DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID;
}
