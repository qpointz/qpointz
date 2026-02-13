package io.qpointz.mill.ai.nlsql.processors.stepback;

/**
 * Factory helpers for Step-Back post-processors.
 */
public class StepBackPostProcessors {

    private StepBackPostProcessors() {
        // utility
    }

    /**
     * Creates a processor that fills defaults and normalizes step-back payloads.
     */
    public static NormalizeStepBackProcessor normalize() {
        return new NormalizeStepBackProcessor();
    }

    /**
     * Creates a processor that keeps clarification flags in sync.
     */
    public static SyncClarificationFlagProcessor syncClarification() {
        return new SyncClarificationFlagProcessor();
    }

    /**
     * Creates a processor that caps clarification questions to a maximum count.
     */
    public static ClarificationLimitProcessor limitQuestions(int maxQuestions) {
        return new ClarificationLimitProcessor(maxQuestions);
    }

    /**
     * Creates a processor that normalizes reasoning-id action keywords.
     */
    public static ReasoningIdActionProcessor reasoningIdAction() {
        return new ReasoningIdActionProcessor();
    }
}
