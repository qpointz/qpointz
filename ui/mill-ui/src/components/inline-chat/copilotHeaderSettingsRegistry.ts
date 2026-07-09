import type {
  AnalysisCopilotAutomationMode,
  InlineChatContextType,
  InlineChatSession,
  AnalysisCopilotSettings,
} from '../../types/inlineChat';

export interface CopilotAutomationModeOption {
  value: AnalysisCopilotAutomationMode;
  label: string;
  description: string;
}

/** Header dropdown definition for a specific inline copilot context type. */
export interface CopilotHeaderMenuConfig {
  menuTitle: string;
  automationMode: {
    label: string;
    options: CopilotAutomationModeOption[];
    getValue: (session: InlineChatSession) => AnalysisCopilotAutomationMode;
  };
  applyAutomationMode: (
    session: InlineChatSession,
    mode: AnalysisCopilotAutomationMode,
  ) => AnalysisCopilotSettings;
}

const analysisCopilotHeaderMenu: CopilotHeaderMenuConfig = {
  menuTitle: 'Analysis copilot',
  automationMode: {
    label: 'Auto',
    getValue: (session) => session.settings['automation.mode'],
    options: [
      {
        value: 'manual',
        label: 'Manual',
        description: 'Preview proposals; you act on artifact strips',
      },
      {
        value: 'apply',
        label: 'Apply',
        description: 'Auto-apply the first SQL proposal in each turn',
      },
      {
        value: 'run',
        label: 'Run',
        description: 'Apply to editor, then run the query',
      },
    ],
  },
  applyAutomationMode: (session, mode) => ({
    ...session.settings,
    'automation.mode': mode,
  }),
};

const copilotHeaderMenus: Partial<Record<InlineChatContextType, CopilotHeaderMenuConfig>> = {
  analysis: analysisCopilotHeaderMenu,
};

/** Returns header menu config for the session context, or null when the copilot has no options. */
export function getCopilotHeaderMenuConfig(
  contextType: InlineChatContextType,
): CopilotHeaderMenuConfig | null {
  return copilotHeaderMenus[contextType] ?? null;
}
