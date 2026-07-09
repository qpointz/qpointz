import type { InlineChatContextType, InlineChatSession } from '../../types/inlineChat';

/** Primary drawer header title for a copilot / inline chat session. */
export function getInlineChatDrawerTitle(session: InlineChatSession): string {
  switch (session.contextType) {
    case 'analysis':
      return 'Analysis copilot';
    case 'model':
      return 'Model assistant';
    case 'knowledge':
      return 'Knowledge assistant';
    default:
      return session.contextLabel || 'Context chat';
  }
}

/** Secondary line under the drawer title (when shown). */
export function getInlineChatDrawerSubtitle(session: InlineChatSession): string | null {
  if (session.contextType === 'analysis') {
    return session.contextLabel.trim() || null;
  }
  return null;
}

export interface InlineChatEmptyStateCopy {
  title: string;
  description: string;
  suggestions: string[];
}

/** Empty-state copy and starter prompts per inline context type. */
export function getInlineChatEmptyState(session: InlineChatSession): InlineChatEmptyStateCopy {
  switch (session.contextType) {
    case 'analysis':
      return {
        title: 'Ask about your query',
        description:
          'Get help optimizing SQL, explaining results, or drafting a new query from your current editor.',
        suggestions: [
          'Optimize this query',
          'Explain what this query does',
          'Rewrite this query',
        ],
      };
    case 'model':
      return {
        title: `Ask about ${session.contextLabel}`,
        description:
          'I can help with schema analysis, data quality, relationships, and documentation.',
        suggestions: [],
      };
    case 'knowledge':
      return {
        title: `Ask about ${session.contextLabel}`,
        description:
          'I can help refine definitions, suggest related concepts, and improve SQL formulas.',
        suggestions: [],
      };
    default:
      return {
        title: `Ask about ${session.contextLabel}`,
        description: 'What would you like to know?',
        suggestions: [],
      };
  }
}

/** True when the session has no user turns yet (greeting-only or empty). */
export function inlineChatShowsEmptyState(session: InlineChatSession): boolean {
  return !session.messages.some((message) => message.role === 'user');
}

/** Mantine color token for inline context accents. */
export function inlineChatAccentColor(contextType: InlineChatContextType): string {
  switch (contextType) {
    case 'analysis':
      return 'teal';
    case 'model':
      return 'teal';
    case 'knowledge':
      return 'grape';
    default:
      return 'gray';
  }
}
