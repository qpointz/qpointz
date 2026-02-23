package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.CallSpecsChatClientBuilders;
import io.qpointz.mill.ai.nlsql.ChatApplication;
import io.qpointz.mill.ai.nlsql.ChatEventProducer;
import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.ai.nlsql.model.MessageRole;
import io.qpointz.mill.ai.nlsql.model.UserChatMessage;
import io.qpointz.mill.ai.nlsql.model.pojo.Chat;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.Reasoner;
import io.qpointz.mill.ai.nlsql.configuration.ValueMappingConfiguration;
import io.qpointz.mill.ai.nlsql.reasoners.DefaultReasoner;
import io.qpointz.mill.ai.nlsql.reasoners.StepBackReasoner;
import io.qpointz.mill.ai.nlsql.repositories.UserChatMessageRepository;
import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Orchestrates NL→SQL processing for user chat inputs and streams responses.
 */
@Service
@Slf4j
@ConditionalOnService("ai-nl2data")
public class ChatProcessor {

    /**
     * Coordinates NL→SQL chat processing: runs the pipeline, persists assistant output, and emits SSE updates.
     * Critical dependencies: shared chat memory, value mapping, and dispatcher wiring; altering these impacts grounding and execution.
     */
    private final UserChatMessageRepository userChatMessageRepository;
    private final MetadataProvider metadataProvider;
    private final DataOperationDispatcher dataOperationDispatcher;
    private final SqlDialect sqlDialect;
    private final ValueMapper valueMapper;
    private final ValueMappingConfiguration configuration;

    /**
     * Constructs the processor with all dependencies required to execute the NL→SQL pipeline.
     */
    public ChatProcessor(MetadataProvider metadataProvider,
                         SqlDialect dialect,
                         DataOperationDispatcher dataOperationDispatcher,
                         UserChatMessageRepository userChatMessageRepository,
                         ValueMapper valueMapper,
                         ValueMappingConfiguration configuration) {
        this.userChatMessageRepository = userChatMessageRepository;
        this.metadataProvider = metadataProvider;
        this.sqlDialect = dialect;
        this.dataOperationDispatcher = dataOperationDispatcher;
        this.valueMapper = valueMapper;
        this.configuration = configuration;
    }

    @AllArgsConstructor
    private static final class ChatSessionEventProducer extends ChatEventProducer {
        private final ChatSession chat;

        @Override
        public <T> void sendEvent(T entity, String event) {
            chat.sendEvent(entity, event);
        }
    }

    /**
     * Processes a user chat request by invoking the NL→SQL application pipeline,
     * persisting the model response, and streaming it back to the client session.
     * Critical path: uses shared chat memory via CallSpecsChatClientBuilders; changes to memory size
     * or builder wiring affect prompt grounding and clarification continuity.
     *
     * @param request incoming chat message (text plus optional content map)
     * @param chat session wrapper with conversation-scoped builders and SSE sink
     */
    public void processRequest(Chat.SendChatMessageRequest request, ChatSession chat) {
        chat.sendEvent(request);

        log.info("Processing chat request conversationId={} message={}", chat.getChat().getId(), request.message());

        val reasoner = createReasoner(chat.getChatBuilders());

        val eventProducer = new ChatSessionEventProducer(chat);

        val application = new ChatApplication(chat.getChatBuilders(),
                                metadataProvider,
                                sqlDialect,
                                dataOperationDispatcher,
                                MessageSelectors.SIMPLE,
                                valueMapper,
                                reasoner,
                                eventProducer);

        val reply = application
                .query(ChatUserRequests.query(request.message()));

        eventProducer.beginProgressEvent("Generating query");
        val resp = reply.asMap();
        eventProducer.endProgressEvent();

        log.debug("Chat reasoning response conversationId={} keys={}", chat.getChat().getId(), resp.keySet());

        val responseBuilder = UserChatMessage.builder()
                .role(MessageRole.CHAT)
                .contentFrom(resp)
                .userChat(chat.getChat());

        val explanation = resp.getOrDefault("explanation", "")
                .toString();

        responseBuilder.message(explanation);

        val um = this.userChatMessageRepository.save(responseBuilder.build());

        chat.sendEvent(um.toPojo());
    }

    /**
     * Creates the appropriate reasoner based on configuration.
     * Defaults to DefaultReasoner if not specified or if "default" is specified.
     * Uses StepBackReasoner if "stepback" is specified.
     *
     * @param chatBuilders chat client builders for reasoning calls
     * @return configured reasoner instance
     */
    private Reasoner createReasoner(CallSpecsChatClientBuilders chatBuilders) {
        val reasonerType = configuration.getReasoner();
        if (reasonerType != null && reasonerType.equalsIgnoreCase("stepback")) {
            log.info("Using StepBackReasoner for NL2SQL processing");
            return new StepBackReasoner(chatBuilders, metadataProvider, MessageSelectors.SIMPLE);
        } else {
            log.info("Using DefaultReasoner for NL2SQL processing");
            return new DefaultReasoner(chatBuilders, metadataProvider, MessageSelectors.SIMPLE);
        }
    }

}
