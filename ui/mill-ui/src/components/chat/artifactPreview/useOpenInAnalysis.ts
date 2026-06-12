import { useCallback } from 'react';
import { useNavigate } from 'react-router';
import type { Message } from '../../../types/chat';

const SUGGESTED_NAME_MAX = 80;

export interface ChatHandoffState {
  chatHandoff?: {
    sql: string;
    suggestedName?: string;
    suggestedDescription?: string;
  };
}

function firstLine(text: string): string {
  return text.split('\n').map((l) => l.trim()).find(Boolean) ?? '';
}

function truncate(text: string, max: number): string {
  const t = text.trim();
  if (t.length <= max) return t;
  return `${t.slice(0, max - 1)}…`;
}

export function deriveSuggestedName(
  sql: string,
  message: Message,
  precedingUserQuestion?: string,
): string {
  if (precedingUserQuestion?.trim()) {
    return truncate(precedingUserQuestion, SUGGESTED_NAME_MAX);
  }
  const prose = firstLine(message.content);
  if (prose) return truncate(prose, SUGGESTED_NAME_MAX);
  if (sql.trim()) return truncate(sql.replace(/\s+/g, ' '), SUGGESTED_NAME_MAX);
  return 'Query from chat';
}

export function deriveSuggestedDescription(
  precedingUserQuestion: string | undefined,
  chatTitle: string | undefined,
  suggestedName: string,
): string | undefined {
  const base = precedingUserQuestion?.trim();
  if (!base) return undefined;
  const withTitle = chatTitle?.trim() ? `${base} — from ${chatTitle.trim()}` : base;
  if (withTitle === suggestedName) return undefined;
  return withTitle;
}

/** Title for expand view — LLM rationale first, then the user question. */
export function deriveExpandTitle(message: Message, precedingUserQuestion?: string): string {
  const prose = firstLine(message.content);
  if (prose) return truncate(prose, SUGGESTED_NAME_MAX);
  if (precedingUserQuestion?.trim()) {
    return truncate(precedingUserQuestion, SUGGESTED_NAME_MAX);
  }
  return 'Query';
}

export function useOpenInAnalysis() {
  const navigate = useNavigate();

  return useCallback(
    (params: {
      sql: string;
      message: Message;
      precedingUserQuestion?: string;
      chatTitle?: string;
    }) => {
      const suggestedName = deriveSuggestedName(params.sql, params.message, params.precedingUserQuestion);
      const suggestedDescription = deriveSuggestedDescription(
        params.precedingUserQuestion,
        params.chatTitle,
        suggestedName,
      );
      const state: ChatHandoffState = {
        chatHandoff: {
          sql: params.sql,
          suggestedName,
          ...(suggestedDescription ? { suggestedDescription } : {}),
        },
      };
      navigate('/analysis', { state });
    },
    [navigate],
  );
}
