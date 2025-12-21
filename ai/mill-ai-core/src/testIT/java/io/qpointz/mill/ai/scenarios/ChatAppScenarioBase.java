package io.qpointz.mill.ai.scenarios;

import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.test.scenario.ActionResult;
import io.qpointz.mill.test.scenario.Scenario;
import io.qpointz.mill.test.scenario.ScenarioRunner;
import io.qpointz.mill.test.scenario.ScenarioTestBase;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
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
@SpringBootTest(classes = {ChatAppScenarioBase.class})
@ActiveProfiles("test-moneta-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class ChatAppScenarioBase extends ScenarioTestBase<ChatAppScenarioContext, ActionResult> {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private MetadataProvider metadataProvider;

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
    private SqlDialect sqlDialect;

    @Override
    protected ScenarioRunner<ChatAppScenarioContext, ActionResult> createRunner(ChatAppScenarioContext context) {
        return new ChatAppScenarioRunner(context);
    }

    @Override
    protected ChatAppScenarioContext createContext(Scenario scenario) {
        return new ChatAppScenarioContext(scenario, this.chatModel, this.metadataProvider, this.sqlDialect, this.dispatcher, this.embeddingModel);
    }

    protected abstract InputStream getScenarioStream(ClassLoader classLoader);

    @Override
    protected Scenario getScenario() {
        try (val ris = this.getScenarioStream(ChatAppScenarioBase.class.getClassLoader()))
            {
                return Scenario.from(ris);
            } catch(IOException e){
                throw new RuntimeException(e);
            }
    }
}