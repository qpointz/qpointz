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
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {GetDataIIntentTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class GetDataIIntentTestIT extends BaseIntentTestIT {

    public GetDataIIntentTestIT(@Autowired ChatModel chatModel,
                                @Autowired MetadataProvider metadataProvider,
                                @Autowired SqlDialect sqlDialect,
                                @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "How many clients?",
            "Count clients by country. Take five countries with most clients.",
            "List clients who trades LME stocks and has loan",
            "List exchanges where most of the clients trading",
            "Get clients in REGULAR segment",
    })
    void roundtrip(String query) {
        val rc = intentSpecs()
                .reasonCall(query)
                .as(ReasoningResponse.class);

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-data", rc.intent());

        val ec = intentSpecs()
                .getChartIntent()
                .getCall(rc)
                .asMap();
        log.info("SQL: ({}) => {}", query, ec.getOrDefault("sql", "NULL"));
        assertTrue(ec.containsKey("data"));

        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        assertTrue(ec.containsKey("query-name"));
        assertTrue(ec.containsKey("explanation"));
    }

    @Test
    void trivialRequest() {
        String query = "how many clients";
        val reason = new ReasoningResponse(query, "get-data", null, List.of(
                new ReasoningResponse.IntentTable("MONETA", "CLIENTS", false)),
            SchemaScope.PARTIAL, SchemaStrategy.PARTIAL_RUNTIME_INJECTION, "en", List.of(), "", List.of(),
                SqlDialect.SqlFeatures.DEFAULT);
        val gd = this.intentSpecs()
                .getDataIntent().getCall(reason);

        val result = gd.asMap();
        assertTrue(result.containsKey("sql"));
        assertTrue(result.containsKey("data"));
    }

    @Test
    void callApplication() {
        this.intentAppTest("How many clients?", "get-data");
    }

}
