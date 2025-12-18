package io.qpointz.mill.ai.scenarios;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.CallSpecsChatClientBuilders;
import io.qpointz.mill.ai.nlsql.ChatApplication;
import io.qpointz.mill.ai.nlsql.ChatEventProducer;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.reasoners.DefaultReasoner;
import io.qpointz.mill.ai.testing.scenario.ActionResult;
import io.qpointz.mill.ai.testing.scenario.Scenario;
import io.qpointz.mill.ai.testing.scenario.ScenarioContext;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class ChatAppScenarioContext extends ScenarioContext<ChatAppScenarioContext, ActionResult> {

    @Getter
    @JsonIgnore
    private final ChatApplication chatApplication;

    @Getter
    private final String scenarioName;

    @Getter
    private final String versionTag;

    public ChatAppScenarioContext(Scenario scenario,
                                  ChatModel chatModel,
                                  MetadataProvider metadataProvider,
                                  SqlDialect sqlDialect,
                                  DataOperationDispatcher dispatcher) {

        this.scenarioName = scenario.name();

        this.versionTag = createVersionTag();
        log.info("Version tag:{}", this.versionTag);

        val chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        val callSpecBuilders = new CallSpecsChatClientBuilders(chatModel,
                chatMemory,
                UUID.randomUUID().toString(),
                null);

        val reasoner = new DefaultReasoner(callSpecBuilders, metadataProvider, MessageSelectors.SIMPLE);

        this.chatApplication = new ChatApplication(
                callSpecBuilders,
                metadataProvider,
                sqlDialect,
                dispatcher,
                MessageSelectors.SIMPLE,
                new DefaultValueMapper(),
                reasoner,
                ChatEventProducer.DEFAULT);
    }

    private String createVersionTag() {
        val ver = System.getenv("REGRESSION_VERSION_TAG");
        return ver != null && !ver.isBlank()
                ? this.versionTag
                : Instant.now().toString();
    }

}
