import type { InlineChatContextType } from '../types/inlineChat';
import { resolveAnalysisCopilotProfileId, resolveGeneralChatAgentProfileId } from './chatPreferences';

/**
 * Resolves the backend profile id for a new inline chat session.
 * Analysis copilot ignores General Chat picker and `VITE_MILL_AI_PROFILE`.
 */
export function resolveInlineChatProfileId(contextType: InlineChatContextType): string {
  if (contextType === 'analysis') {
    return resolveAnalysisCopilotProfileId();
  }
  return resolveGeneralChatAgentProfileId();
}
