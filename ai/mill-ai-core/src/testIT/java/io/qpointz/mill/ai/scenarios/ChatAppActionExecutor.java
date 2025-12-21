package io.qpointz.mill.ai.scenarios;

import io.qpointz.mill.test.scenario.Action;
import io.qpointz.mill.test.scenario.ActionOutcome;

public interface ChatAppActionExecutor {
    ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception;
}
