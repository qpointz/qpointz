import type { ComponentType } from 'react';
import type { ChatExpandPayload } from './useChatExpand';
import { SqlDataExpandedView } from './SqlDataExpandedView';

type ExpandComponent = ComponentType<{ payload: ChatExpandPayload; onClose: () => void }>;

const expandRegistry: Partial<Record<string, ExpandComponent>> = {
  'sql-data-composite': SqlDataExpandedView,
};

export function resolveExpandComponent(kind: string, chatType: string): ExpandComponent | null {
  if (chatType !== 'general') return null;
  return expandRegistry[kind] ?? null;
}
