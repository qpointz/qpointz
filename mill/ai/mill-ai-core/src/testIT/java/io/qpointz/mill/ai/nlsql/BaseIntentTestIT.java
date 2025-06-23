package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.chat.ChatCallSpec;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class BaseIntentTestIT  extends BaseIntegrationTestIT {


    @Getter(AccessLevel.PROTECTED)
    private final CallSpecs callSpecs;

    @Getter(AccessLevel.PROTECTED)
    private final MetadataProvider metadataProvider;

    @Getter(AccessLevel.PROTECTED)
    private final DataOperationDispatcher dispatcher;
    private final ChatModel chatModel;

    @Getter(AccessLevel.PROTECTED)
    private final CallSpecsChatClientBuilders callSpecBuilders;
    private final MessageWindowChatMemory chatMemory;

    protected CallSpecs callSpecs() {
        return this.callSpecs;
    }

    protected BaseIntentTestIT(ChatModel model,
                               MetadataProvider metadataProvider,
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
        this.callSpecs = new CallSpecs(metadataProvider, this.callSpecBuilders, dispatcher, Set.of());
    }

    protected void logPrompt(ChatCallSpec gd) {
        val sb = new StringBuilder();
        gd.getSelectedMessages().stream().forEach(k-> {
            sb.append(k.getText());
            sb.append("--------------");
        });
        log.info("\n{}", sb.toString());
    }

    protected void intentAppTest(String query, String expectedIntent) {
        val app = new ChatApplication(
                new CallSpecsChatClientBuilders(this.chatModel,
                        this.chatMemory,
                        UUID.randomUUID().toString()),
                this.metadataProvider,
                this.dispatcher,
                Set.of());
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
