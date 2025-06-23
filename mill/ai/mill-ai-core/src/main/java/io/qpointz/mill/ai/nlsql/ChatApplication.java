package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatCallSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Slf4j
public class ChatApplication {

    @Getter
    private final MetadataProvider metadataProvider;

    @Getter
    private final DataOperationDispatcher dispatcher;

    @Getter
    private final CallSpecs callSpec;

    private final Map<String, BiFunction<ReasoningResponse, CallSpecs, ChatCallSpec>> intentsRegistry;

    private final Set<Integer> promptHashes;

    public ChatApplication(CallSpecsChatClientBuilders chatBuilders, MetadataProvider metadataProvider, DataOperationDispatcher dispatcher) {
        this(chatBuilders, metadataProvider, dispatcher, Set.of());
    }

    public ChatApplication(CallSpecsChatClientBuilders chatBuilders, MetadataProvider metadataProvider, DataOperationDispatcher dispatcher, Set<Integer> promptHashes) {
        this.metadataProvider = metadataProvider;
        this.dispatcher = dispatcher;
        this.callSpec = new CallSpecs(metadataProvider, chatBuilders , dispatcher, promptHashes);
        this.intentsRegistry = createIntentsRegistry();
        this.promptHashes = promptHashes;
    }

    public ChatCallSpec reasonSpec(String query) {
        return this.getCallSpec()
                .reasonSpec(query);
    }

    public ChatCall reason(String question) {
        return this.reasonSpec(question)
                .call();
    }

    public ChatCallSpec querySpec(String query) {
        val reasoningResponse =  this.reason(query)
                .as(ReasoningResponse.class);
        return getIntentCallSpec(reasoningResponse);
    }

    public ChatCall query(String query) {
        return querySpec(query)
                .call();
    }

    private Map<String, BiFunction<ReasoningResponse, CallSpecs, ChatCallSpec>> createIntentsRegistry() {
        return Map.of(
                "get-data", (r, c) -> c.getDataSpec(r),
                "get-chart", (r, c) -> c.getChartSpec(r),
                "follow-up", (r, c) -> c.followUpSpec(r),
                "enrich-model", (r,c) -> c.enrichModelSpec(r),
                "explain", (r,c) -> c.explainSpec(r),
                "unsupported", (r,c) -> c.unsuported(r)
        );
    }

    private ChatCallSpec getIntentCallSpec(ReasoningResponse reasoningResponse) {
        log.info("Resolving intent '{}'", reasoningResponse.intent());
        log.info("Reasoning response:'{}'", reasoningResponse);
        val intent = reasoningResponse
                .intent()
                .toLowerCase();
        return this.intentsRegistry.get(intent)
                .apply(reasoningResponse, callSpec);

    }

}
