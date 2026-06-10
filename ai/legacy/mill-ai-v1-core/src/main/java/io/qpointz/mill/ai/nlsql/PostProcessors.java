package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.processors.*;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;

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

    public static SubmitQueryProcessor submitQueryProcessor(DataOperationDispatcher dispatcher, int i, ChatEventProducer eventProducer) {
        return new SubmitQueryProcessor(dispatcher, 10, eventProducer);
    }

    public static ExecuteQueryProcessor executeQueryProcessor(DataOperationDispatcher dispatcher, ChatEventProducer eventProducer) {
        return new ExecuteQueryProcessor(dispatcher, eventProducer);
    }

    public static MapValueProcessor mapValueProcessor(ValueMapper valueMapper, ChatEventProducer eventProducer) {
        return new MapValueProcessor(valueMapper, eventProducer);
    }

    public static RefineIntentProcessor refineProcessor(ReasoningResponse response, IntentSpecs specs) {
        return new RefineIntentProcessor(response, specs);
    }

}
