import { describe, expect, it } from 'vitest';
import {
  shouldAutoApplyOnArrival,
  shouldAutoRunAfterApply,
} from '../analysisCopilotAutomation';

describe('analysisCopilotAutomation', () => {
  it('should not auto-apply in manual mode', () => {
    expect(shouldAutoApplyOnArrival('manual')).toBe(false);
    expect(shouldAutoRunAfterApply('manual')).toBe(false);
  });

  it('should auto-apply but not run in apply mode', () => {
    expect(shouldAutoApplyOnArrival('apply')).toBe(true);
    expect(shouldAutoRunAfterApply('apply')).toBe(false);
  });

  it('should auto-apply and run in run mode', () => {
    expect(shouldAutoApplyOnArrival('run')).toBe(true);
    expect(shouldAutoRunAfterApply('run')).toBe(true);
  });
});
