package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

import static io.qpointz.mill.ai.nlsql.MessageSpecs.*;
import static io.qpointz.mill.ai.nlsql.PostProcessors.*;

@Slf4j
public class IntentSpecs {

    @Getter
    private final MetadataProvider metadataProvider;

    @Getter
    private final CallSpecsChatClientBuilders chatBuilders;

    @Getter
    private final DataOperationDispatcher dispatcher;

    @Getter
    private final List<IntentSpec> intents;

    @Getter
    private final MessageSelector messageSelector;

    @Getter
    private final SqlDialect sqlDialect;

    @Getter
    private final ValueMapper valueMapper;

    @Getter
    private final ChatEventProducer eventProducer;

    public IntentSpecs(MetadataProvider metadataProvider,
                       SqlDialect sqlDialect,
                       CallSpecsChatClientBuilders chatBuilders,
                       DataOperationDispatcher dispatcher,
                       MessageSelector messageSelector,
                       ValueMapper valueMapper,
                       ChatEventProducer eventProducer) {
        this.metadataProvider = metadataProvider;
        this.chatBuilders = chatBuilders;
        this.dispatcher = dispatcher;
        this.messageSelector = messageSelector;
        this.sqlDialect = sqlDialect;
        this.valueMapper = valueMapper;
        this.eventProducer = eventProducer;
        this.intents  = List.of(
                getDataIntent(),
                getChartIntent(),
                getExplainIntent(),
                getEnrichModelIntent(),
                getRefineIntent(),
                getDoConversationIntent(),
                getUnsuportedInten()
        );
    }

    public static final String GET_DATA_INTENT_KEY = "get-data";
    public IntentSpec getDataIntent() {
        return IntentSpec.builder(GET_DATA_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        getData(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        mapValueProcessor(this.valueMapper, this.eventProducer),
                        submitQueryProcessor(dispatcher, 10, this.eventProducer),
                        retainReasoning(reason)))
                .build();
    }

    public static final String GET_CHART_INTENT_KEY = "get-chart";
    public IntentSpec getChartIntent() {
        return IntentSpec.builder(GET_CHART_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        getChart(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        mapValueProcessor(this.valueMapper, this.eventProducer),
                        executeQueryProcessor(dispatcher, eventProducer),
                        retainReasoning(reason)))
                .build();
    }

    public static final String EXPLAIN_INTENT_KEY = "explain";
    public IntentSpec getExplainIntent() {
        return IntentSpec.builder(EXPLAIN_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        explain(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        retainReasoning(reason)))
                .build();
    }

    public static final String ENRICH_MODEL_INTENT_KEY = "enrich-model";
    public IntentSpec getEnrichModelIntent() {
        return IntentSpec.builder(ENRICH_MODEL_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        enrichModel(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        retainReasoning(reason)))
                .build();
    }

    public static final String REFINE_QUERY_INTENT_KEY = "refine";
    public IntentSpec getRefineIntent() {
        return IntentSpec.builder(REFINE_QUERY_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        refineQuery(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        retainReasoning(reason),
                        refineProcessor(reason, this)))
                .build();
    }

    public static final String DO_CONVERSATION_INTENT_KEY = "do-conversation";
    public IntentSpec getDoConversationIntent() {
        return IntentSpec.builder(DO_CONVERSATION_INTENT_KEY)
                .callFunc(reason -> new IntentCall(reason, this, this.chatBuilders.conversationChat(),
                        doConversation(reason, metadataProvider, sqlDialect),
                        this.messageSelector))
                .postProcessorFunc(reason -> List.of(
                        retainReasoning(reason)))
                .build();
    }

    public static final String UNSUPPORTED_INTENT_KEY = "unsupported";
    public IntentSpec getUnsuportedInten() {
        return IntentSpec.builder(UNSUPPORTED_INTENT_KEY)
                .callFunc(UnsupportedCall::new)
                .build();
    }

    public IntentSpec getIntent(String intent) {
        return this.getIntents().stream()
                .filter(k-> k.getKey().equals(intent))
                .findFirst()
                .orElseThrow(() -> new MillRuntimeException("Unknown intent: " + intent));
    }

    public ChatCall getIntentCall(ReasoningResponse reasoningResponse) {
        log.info("Resolving intent '{}'", reasoningResponse.intent());
        log.info("Reasoning response:'{}'", reasoningResponse);
        val intent = reasoningResponse
                .intent()
                .toLowerCase();
        return this
                .getIntent(intent)
                .getCall(reasoningResponse);
    }

}