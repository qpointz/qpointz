package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class BaseIntentTestIT  extends BaseIntegrationTestIT {


    @Getter(AccessLevel.PROTECTED)
    @Accessors(fluent = true)
    private final IntentSpecs intentSpecs;

    @Getter(AccessLevel.PROTECTED)
    private final MetadataProvider metadataProvider;

    @Getter(AccessLevel.PROTECTED)
    private final DataOperationDispatcher dispatcher;
    private final ChatModel chatModel;

    @Getter(AccessLevel.PROTECTED)
    private final CallSpecsChatClientBuilders callSpecBuilders;

    private final MessageWindowChatMemory chatMemory;

    @Getter(AccessLevel.PROTECTED)
    private final SqlDialect sqlDialect;

    protected BaseIntentTestIT(ChatModel model,
                               MetadataProvider metadataProvider,
                               SqlDialect sqlDialect,
                               DataOperationDispatcher dispatcher) {

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        this.metadataProvider = metadataProvider;
        this.dispatcher = dispatcher;
        this.chatModel = model;
        this.callSpecBuilders = new CallSpecsChatClientBuilders(
                model, this.chatMemory, UUID.randomUUID().toString());
        this.sqlDialect = sqlDialect;
        this.intentSpecs = new IntentSpecs(metadataProvider, sqlDialect, this.callSpecBuilders, dispatcher, MessageSelectors.SIMPLE);
    }

    protected void intentAppTest(String query, String expectedIntent) {
        val app = new ChatApplication(
                new CallSpecsChatClientBuilders(this.chatModel,
                        this.chatMemory,
                        UUID.randomUUID().toString()),
                this.metadataProvider,
                this.sqlDialect,
                this.dispatcher,
                MessageSelectors.SIMPLE);
        val reason = app.reason(query)
                .as(ReasoningResponse.class);
        assertEquals(expectedIntent, reason.intent(), "Intent missmatch after reasoning");

        val call = app.query(query)
                .asMap();

        val callReason = JsonUtils.defaultJsonMapper()
                .convertValue(call.get("reasoning"), ReasoningResponse.class);


        assertEquals(expectedIntent, callReason.intent(), "Call intent missmatch");
    }

}
