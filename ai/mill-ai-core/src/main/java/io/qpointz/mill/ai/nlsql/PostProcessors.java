package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.processors.*;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;

public class PostProcessors {

    public static RetainQueryProcessor retainQuery(String query) {
        return new RetainQueryProcessor(query);
    }

    public static CheckIntentPresentProcessor checkIntentPresent() {
        return new CheckIntentPresentProcessor();
    }

    public static RetainReasoningProcessor retainReasoning(ReasoningResponse reason) {
        return new RetainReasoningProcessor(reason);
    }

    public static SubmitQueryProcessor submitQueryProcessor(DataOperationDispatcher dispatcher, int i) {
        return new SubmitQueryProcessor(dispatcher, 10);
    }

    public static ExecuteQueryProcessor executeQueryProcessor(DataOperationDispatcher dispatcher) {
        return new ExecuteQueryProcessor(dispatcher);
    }

    public static MapValueProcessor mapValueProcessor(ValueMapper valueMapper) {
        return new MapValueProcessor(valueMapper);
    }

    public static RefineIntentProcessor refineProcessor(ReasoningResponse response, IntentSpecs specs) {
        return new RefineIntentProcessor(response, specs);
    }

}
