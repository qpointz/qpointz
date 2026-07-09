import { describe, expect, it } from 'vitest';
import { getCopilotHeaderMenuConfig } from '../copilotHeaderSettingsRegistry';
import { DEFAULT_ANALYSIS_COPILOT_SETTINGS } from '../../../types/inlineChat';
import type { InlineChatSession } from '../../../types/inlineChat';

function analysisSession(overrides: Partial<InlineChatSession> = {}): InlineChatSession {
  return {
    id: 's1',
    chatId: null,
    contextType: 'analysis',
    contextId: 'q1',
    contextLabel: 'Query',
    messages: [],
    isLoading: false,
    createdAt: 0,
    thinkingMessage: null,
    settings: { ...DEFAULT_ANALYSIS_COPILOT_SETTINGS },
    ...overrides,
  };
}

describe('copilotHeaderSettingsRegistry', () => {
  it('should expose Analysis copilot automation radio options', () => {
    const config = getCopilotHeaderMenuConfig('analysis');
    expect(config?.menuTitle).toBe('Analysis copilot');
    expect(config?.automationMode.options.map((o) => o.value)).toEqual(['manual', 'apply', 'run']);
    expect(config?.automationMode.getValue(analysisSession())).toBe('manual');
  });

  it('should return null for copilots without header settings', () => {
    expect(getCopilotHeaderMenuConfig('model')).toBeNull();
    expect(getCopilotHeaderMenuConfig('knowledge')).toBeNull();
  });

  it('should apply automation mode patches for Analysis settings', () => {
    const config = getCopilotHeaderMenuConfig('analysis');
    const session = analysisSession();
    const next = config?.applyAutomationMode(session, 'run');
    expect(next).toEqual({ 'automation.mode': 'run' });
  });
});
