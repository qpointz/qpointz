import { afterEach, describe, expect, it, vi } from 'vitest';
import { resolveInlineChatProfileId } from '../inlineProfileRouting';
import { ANALYSIS_COPILOT_PROFILE_ID } from '../chatPreferences';

describe('resolveInlineChatProfileId', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
  });

  it('should return analysis-copilot for Analysis inline chats', () => {
    expect(resolveInlineChatProfileId('analysis')).toBe(ANALYSIS_COPILOT_PROFILE_ID);
  });

  it('should keep General Chat profile resolution for non-analysis hosts', () => {
    vi.stubEnv('VITE_MILL_AI_PROFILE', 'schema-exploration');
    expect(resolveInlineChatProfileId('model')).toBe('schema-exploration');
    expect(resolveInlineChatProfileId('knowledge')).toBe('schema-exploration');
  });

  it('should not let General Chat env override Analysis copilot profile', () => {
    vi.stubEnv('VITE_MILL_AI_PROFILE', 'hello-world');
    expect(resolveInlineChatProfileId('analysis')).toBe('analysis-copilot');
  });
});
