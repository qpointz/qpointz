package io.qpointz.mill.ai.scenarios;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.*;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import io.qpointz.mill.ai.nlsql.components.DefaultValueRepository;
import io.qpointz.mill.ai.nlsql.components.VectorStoreValueMapper;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.reasoners.DefaultReasoner;
import io.qpointz.mill.ai.nlsql.reasoners.StepBackReasoner;
import io.qpointz.mill.test.scenario.ActionResult;
import io.qpointz.mill.test.scenario.Scenario;
import io.qpointz.mill.test.scenario.ScenarioContext;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.time.Instant;
import java.util.List;
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
                                  DataOperationDispatcher dispatcher,
                                  EmbeddingModel embeddingModel) {

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

        val reasoner = createReasoner(scenario, callSpecBuilders,
                metadataProvider, MessageSelectors.SIMPLE);

        val valueMapper = createValueMapper(scenario, embeddingModel);

        this.chatApplication = new ChatApplication(
                callSpecBuilders,
                metadataProvider,
                sqlDialect,
                dispatcher,
                MessageSelectors.SIMPLE,
                valueMapper,
                reasoner,
                ChatEventProducer.DEFAULT);
    }

    private Reasoner createReasoner(Scenario scenario, CallSpecsChatClientBuilders callSpecBuilders, MetadataProvider metadataProvider, MessageSelector messageSelector) {
        val reasonerName = scenario.parameters()
                .getOrDefault("reasoner","default")
                .toString()
                .toLowerCase();

        return switch (reasonerName) {
            case "default" -> new DefaultReasoner(callSpecBuilders, metadataProvider, messageSelector);
            case "step-back" -> new StepBackReasoner(callSpecBuilders,metadataProvider, messageSelector);
            default -> throw new RuntimeException("Unknown reasoner:"+reasonerName);
        };
    }

    private String createVersionTag() {
        val ver = System.getenv("REGRESSION_VERSION_TAG");
        return ver != null && !ver.isBlank()
                ? this.versionTag
                : Instant.now().toString();
    }

    private ValueMapper createValueMapper(Scenario scenario, EmbeddingModel embeddingModel) {
        val valueMappingDocuments = ((List)scenario.parameters()
                .getOrDefault("value-mapping", List.of()))
                .stream()
                .map(k-> JsonUtils.defaultJsonMapper().convertValue(k, ValueRepository.ValueDocument.class))
                .toList();

        if (valueMappingDocuments.isEmpty()) {
            return new DefaultValueMapper();
        }

        val vectorStore = SimpleVectorStore
                .builder(embeddingModel)
                .build();
        val valueRepository = new DefaultValueRepository(vectorStore);

        valueRepository.ingest(valueMappingDocuments);

        return new VectorStoreValueMapper(valueRepository);

    }
}
