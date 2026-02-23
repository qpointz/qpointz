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
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnService("ai-nl2data")
public class ChatProcessor {

    private final UserChatMessageRepository userChatMessageRepository;
    private final MetadataService metadataService;
    private final DataOperationDispatcher dataOperationDispatcher;
    private final SqlDialect sqlDialect;
    private final ValueMapper valueMapper;
    private final ValueMappingConfiguration configuration;

    public ChatProcessor(MetadataService metadataService,
                         SqlDialect dialect,
                         DataOperationDispatcher dataOperationDispatcher,
                         UserChatMessageRepository userChatMessageRepository,
                         ValueMapper valueMapper,
                         ValueMappingConfiguration configuration) {
        this.userChatMessageRepository = userChatMessageRepository;
        this.metadataService = metadataService;
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

    public void processRequest(Chat.SendChatMessageRequest request, ChatSession chat) {
        chat.sendEvent(request);

        log.info("Processing chat request conversationId={} message={}", chat.getChat().getId(), request.message());

        val reasoner = createReasoner(chat.getChatBuilders());

        val eventProducer = new ChatSessionEventProducer(chat);

        val application = new ChatApplication(chat.getChatBuilders(),
                                metadataService,
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

    private Reasoner createReasoner(CallSpecsChatClientBuilders chatBuilders) {
        val reasonerType = configuration.getReasoner();
        if (reasonerType != null && reasonerType.equalsIgnoreCase("stepback")) {
            log.info("Using StepBackReasoner for NL2SQL processing");
            return new StepBackReasoner(chatBuilders, metadataService, MessageSelectors.SIMPLE);
        } else {
            log.info("Using DefaultReasoner for NL2SQL processing");
            return new DefaultReasoner(chatBuilders, metadataService, MessageSelectors.SIMPLE);
        }
    }
}
