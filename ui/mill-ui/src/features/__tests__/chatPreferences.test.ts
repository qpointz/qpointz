import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID,
  GENERAL_CHAT_AGENT_PROFILE_SS_KEY,
  resolveGeneralChatAgentProfileId,
} from '../chatPreferences';

describe('resolveGeneralChatAgentProfileId', () => {
  afterEach(() => {
    sessionStorage.clear();
    vi.unstubAllEnvs();
  });

  it('shouldPreferExplicitSelectedId', () => {
    expect(resolveGeneralChatAgentProfileId({ selectedId: 'schema-authoring' })).toBe('schema-authoring');
  });

  it('shouldUseSessionStorageWhenNoPickerSelection', () => {
    sessionStorage.setItem(GENERAL_CHAT_AGENT_PROFILE_SS_KEY, 'hello-world');
    expect(resolveGeneralChatAgentProfileId()).toBe('hello-world');
  });

  it('shouldUseViteEnvWhenNoSessionValue', () => {
    vi.stubEnv('VITE_MILL_AI_PROFILE', 'schema-exploration');
    expect(resolveGeneralChatAgentProfileId()).toBe('schema-exploration');
  });

  it('shouldDefaultToDataAnalysisWhenNothingConfigured', () => {
    expect(resolveGeneralChatAgentProfileId()).toBe(DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID);
    expect(DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID).toBe('data-analysis');
  });
});
