import type { AnalysisCopilotAutomationMode } from '../../types/inlineChat';

/** True when the host should auto-apply the first SQL proposal in a turn. */
export function shouldAutoApplyOnArrival(mode: AnalysisCopilotAutomationMode): boolean {
  return mode === 'apply' || mode === 'run';
}

/** True when the host should execute after an apply (manual or automatic). */
export function shouldAutoRunAfterApply(mode: AnalysisCopilotAutomationMode): boolean {
  return mode === 'run';
}
