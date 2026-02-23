package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.reasoners.DefaultReasoner;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.service.MetadataService;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@EnableAutoConfiguration
public abstract class BaseIntentTestIT extends BaseIntegrationTestIT {

    @Getter(AccessLevel.PROTECTED)
    @Accessors(fluent = true)
    private final IntentSpecs intentSpecs;

    @Getter(AccessLevel.PROTECTED)
    private final MetadataService metadataService;

    @Getter(AccessLevel.PROTECTED)
    private final DataOperationDispatcher dispatcher;
    private final ChatModel chatModel;

    @Getter(AccessLevel.PROTECTED)
    private final CallSpecsChatClientBuilders callSpecBuilders;

    private final MessageWindowChatMemory chatMemory;

    @Getter(AccessLevel.PROTECTED)
    private final SqlDialect sqlDialect;

    @Getter(AccessLevel.PROTECTED)
    private final DefaultReasoner reasoner;

    @Getter(AccessLevel.PROTECTED)
    private final MessageSelector messageSelector;

    protected BaseIntentTestIT(ChatModel model,
                               MetadataService metadataService,
                               SqlDialect sqlDialect,
                               DataOperationDispatcher dispatcher) {

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        this.metadataService = metadataService;
        this.dispatcher = dispatcher;
        this.chatModel = model;
        this.callSpecBuilders = new CallSpecsChatClientBuilders(
                model, this.chatMemory, UUID.randomUUID().toString(), null);
        this.sqlDialect = sqlDialect;
        this.messageSelector = MessageSelectors.SIMPLE;
        this.intentSpecs = new IntentSpecs(metadataService, sqlDialect, this.callSpecBuilders, dispatcher, this.messageSelector, new DefaultValueMapper(), ChatEventProducer.DEFAULT);
        this.reasoner = new DefaultReasoner(this.callSpecBuilders, metadataService, MessageSelectors.SIMPLE);
    }

    protected void intentAppTest(String query, String expectedIntent) {
        val app = new ChatApplication(
                new CallSpecsChatClientBuilders(this.chatModel,
                        this.chatMemory,
                        UUID.randomUUID().toString(),
                        null),
                this.metadataService,
                this.sqlDialect,
                this.dispatcher,
                MessageSelectors.SIMPLE,
                new DefaultValueMapper(),
                this.getReasoner(),
                ChatEventProducer.DEFAULT);
        val reason = app.reason(ChatUserRequests.query(query))
                        .reasoningResponse();
        assertEquals(expectedIntent, reason.intent(), "Intent missmatch after reasoning");

        val call = app.query(ChatUserRequests.query(query))
                .asMap();

        val callReason = JsonUtils.defaultJsonMapper()
                .convertValue(call.get("reasoning"), ReasoningResponse.class);

        assertEquals(expectedIntent, callReason.intent(), "Call intent missmatch");
    }
}
