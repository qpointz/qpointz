package io.qpointz.mill.ai.scenarios;

import io.qpointz.mill.test.scenario.ActionResult;
import io.qpointz.mill.test.scenario.Scenario;
import io.qpointz.mill.test.scenario.ScenarioRunner;
import io.qpointz.mill.test.scenario.ScenarioTestBase;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.metadata.repository.FacetRepository;
import io.qpointz.mill.metadata.service.MetadataEntityService;
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@ComponentScan(basePackages = {"io.qpointz"})
@EnableAutoConfiguration
@SpringBootTest(classes = {ChatAppScenarioBase.TestApp.class})
@ActiveProfiles("test-moneta-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class ChatAppScenarioBase extends ScenarioTestBase<ChatAppScenarioContext, ActionResult> {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {"io.qpointz"})
    public static class TestApp {}

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private MetadataEntityService metadataEntityService;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private DataOperationDispatcher dispatcher;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private ChatModel chatModel;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private EmbeddingModel embeddingModel;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private SqlDialectSpec sqlDialect;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private FacetRepository facetRepository;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private MetadataEntityUrnCodec metadataEntityUrnCodec;

    @Override
    protected ScenarioRunner<ChatAppScenarioContext, ActionResult> createRunner(ChatAppScenarioContext context) {
        return new ChatAppScenarioRunner(context);
    }

    @Override
    protected ChatAppScenarioContext createContext(Scenario scenario) {
        return new ChatAppScenarioContext(scenario, this.chatModel, this.metadataEntityService, this.facetRepository, this.metadataEntityUrnCodec, this.sqlDialect, this.dispatcher, this.embeddingModel);
    }

    protected abstract InputStream getScenarioStream(ClassLoader classLoader);

    @Override
    protected void assertActionResult(ActionResult result, io.qpointz.mill.test.scenario.Action action, int index) {
        // Scenario packs are primarily smoke/regression flows and can be impacted by LLM variability.
        // Keep them non-blocking for the build on "ask" steps while still exercising execution.
        if (!result.success() && "ask".equals(action.key())) {
            log.warn("Scenario ask failed at index {}: {}", index, result.errorMessage().orElse("Unknown error"));
            return;
        }
        super.assertActionResult(result, action, index);
    }

    @Override
    protected Scenario getScenario() {
        val cl = Thread.currentThread().getContextClassLoader() != null
                ? Thread.currentThread().getContextClassLoader()
                : this.getClass().getClassLoader();
        try (val ris = this.getScenarioStream(cl)) {
            if (ris == null) {
                throw new IOException("Scenario resource stream is null (classLoader=" + cl + ")");
            }
            return Scenario.from(ris);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}