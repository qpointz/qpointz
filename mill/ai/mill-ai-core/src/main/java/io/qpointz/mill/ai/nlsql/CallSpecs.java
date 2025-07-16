//package io.qpointz.mill.ai.nlsql;
//
//import io.qpointz.mill.ai.chat.ChatCall;
//import io.qpointz.mill.ai.chat.ChatCallSpec;
//import io.qpointz.mill.ai.chat.ChatClientBuilder;
//import io.qpointz.mill.ai.chat.messages.MessageList;
//import io.qpointz.mill.ai.chat.messages.MessageSelector;
//import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import io.qpointz.mill.services.metadata.MetadataProvider;
//import io.qpointz.mill.utils.JsonUtils;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import static io.qpointz.mill.ai.nlsql.MessageSpecs.*;
//import static io.qpointz.mill.ai.nlsql.PostProcessors.*;
//
//@AllArgsConstructor
//public class CallSpecs {
//
//    @Getter
//    private final MetadataProvider metadataProvider;
//
//    @Getter
//    private final CallSpecsChatClientBuilders chatBuilders;
//
//    @Getter
//    private final DataOperationDispatcher dispatcher;
//
//    @Getter
//    private final Set<Integer> promptHashes;
//
//
//    public ChatCallSpec reasonSpec(String query) {
//        return ChatCallSpec.builder()
//                    .chatClientBuilder(chatBuilders.reasoningChat())
//                    .messages(reason(query, metadataProvider))
//                    .postprocessors(List.of(
//                        checkIntentPresent(),
//                        retainQuery(query)
//                    ))
//                    .promptHashes(this.promptHashes)
//                .build();
//    }
//
//    public ChatCallSpec getDataSpec(ReasoningResponse reasoningResponse) {
//        return ChatCallSpec.builder()
//                .chatClientBuilder(chatBuilders.conversationChat())
//                .messages(getData(reasoningResponse, metadataProvider))
//                .postprocessors(List.of(
//                        submitQueryProcessor(dispatcher, 10),
//                        retainReasoning(reasoningResponse)
//                ))
//                .promptHashes(this.promptHashes)
//                .build();
//    }
//
//    public ChatCallSpec getChartSpec(ReasoningResponse reasoningResponse) {
//        return ChatCallSpec.builder()
//                .chatClientBuilder(chatBuilders.conversationChat())
//                .messages(getChart(reasoningResponse, metadataProvider))
//                .postprocessors(List.of(
//                        executeQueryProcessor(dispatcher),
//                        retainReasoning(reasoningResponse)
//                ))
//                .promptHashes(this.promptHashes)
//                .build();
//    }
//
//
//    public ChatCallSpec explainSpec(ReasoningResponse reasoningResponse) {
//        return ChatCallSpec.builder()
//                .chatClientBuilder(chatBuilders.conversationChat())
//                .messages(explain(reasoningResponse, metadataProvider))
//                .postprocessors(List.of(
//                        retainReasoning(reasoningResponse)
//                ))
//                .promptHashes(this.promptHashes)
//                .build();
//    }
//
//    public ChatCallSpec enrichModelSpec(ReasoningResponse reasoningResponse) {
//        return ChatCallSpec.builder()
//                .chatClientBuilder(chatBuilders.conversationChat())
//                .messages(enrichModel(reasoningResponse, metadataProvider))
//                .postprocessors(List.of(
//                        retainReasoning(reasoningResponse)
//                ))
//                .promptHashes(this.promptHashes)
//                .build();
//    }
//
//    public ChatCallSpec followUpSpec(ReasoningResponse reasoningResponse) {
//        return ChatCallSpec.builder()
//                .chatClientBuilder(chatBuilders.conversationChat())
//                .messages(followUp(reasoningResponse, metadataProvider))
//                .postprocessors(List.of(
//                        retainReasoning(reasoningResponse)
//                ))
//                .promptHashes(this.promptHashes)
//                .build();
//    }
//
//    private class UnsuportedCall implements ChatCall {
//
//        @Override
//        public Map<String, Object> asMap() {
//            return Map.of();
//        }
//
//        @Override
//        public <T> T as(Class<T> valueType) {
//            return ChatCall.super.as(valueType);
//        }
//    }
//
//
//    public class UnsuportedSpec extends ChatCallSpec {
//
//        @Getter
//        private final ReasoningResponse reason;
//
//        public UnsuportedSpec(ReasoningResponse reason, ChatClientBuilder chatClientBuilder, MessageSelector messageSelector, Set<Integer> hashes) {
//            super(chatBuilders.conversationChat(), new MessageList(List.of()), messageSelector, List.of(retainReasoning(reason)), hashes);
//            this.reason = reason;
//        }
//
//        @Override
//        public ChatCall call() {
//            return new UnsupportedCall(reason);
//        }
//    }
//
//    public class UnsupportedCall implements ChatCall {
//
//        private final ReasoningResponse reason;
//
//        public UnsupportedCall(ReasoningResponse reason) {
//            this.reason = reason;
//        }
//
//        @Override
//        public Map<String, Object> asMap() {
//            return JsonUtils.defaultJsonMapper().convertValue(reason, Map.class);
//        }
//    }
//
//    public ChatCallSpec unsuported(ReasoningResponse r) {
//        return new UnsuportedSpec(r, chatBuilders.conversationChat(), null, this.promptHashes);
//    }
//}
