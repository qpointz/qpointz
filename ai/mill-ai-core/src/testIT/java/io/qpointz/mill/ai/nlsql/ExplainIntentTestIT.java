package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {ExplainIntentTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class ExplainIntentTestIT extends BaseIntentTestIT {

    public ExplainIntentTestIT(@Autowired ChatModel chatModel,
                               @Autowired MetadataProvider metadataProvider,
                               @Autowired SqlDialect sqlDialect,
                               @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Describe model",
            "Which tables are contains loan information",
            "Which data contains in ACCOUNT tables",
            "What is client_id attribute and in which tables it exists"
    })
    void roundtrip(String query) {
        val rc = intentSpecs()
                .reasonCall(query)
                .as(ReasoningResponse.class);
        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("explain", rc.intent());
        val spec = intentSpecs()
                .getExplainIntent()
                .getCall(rc);

        val ec = spec
                .asMap();
        //logPrompt(spec);
        log.info("Description:");
        log.info("==============================================");
        log.info("\n{}", ec.get("description"));

        assertTrue(ec.containsKey("description"));
        assertTrue(ec.containsKey("title"));


        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        assertTrue(ec.containsKey("explanation"));
    }

    @Test
    void callApplication() {
        this.intentAppTest("Describe model",
                "explain");
    }

}

