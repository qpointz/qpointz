package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlFeatures;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.metadata.repository.FacetRepository;
import io.qpointz.mill.metadata.service.MetadataEntityService;
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec;
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
                                @Autowired MetadataEntityService metadataEntityService,
                                @Autowired FacetRepository facetRepository,
                                @Autowired MetadataEntityUrnCodec urnCodec,
                                @Autowired SqlDialectSpec sqlDialect,
                                @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataEntityService, facetRepository, urnCodec, sqlDialect, dispatcher);
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
        val rc = this.getReasoner()
                .reason(ChatUserRequests.query(query))
                .reasoningResponse();

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-data", rc.intent());

        val ec = intentSpecs()
                .getDataIntent()
                .getCall(rc)
                .asMap();
        log.info("SQL: ({}) => {}", query, ec.getOrDefault("sql", "NULL"));
        assertTrue(ec.containsKey("sql"));

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
                SqlFeatures.DEFAULT);
        val gd = this.intentSpecs()
                .getDataIntent().getCall(reason);

        val result = gd.asMap();
        assertTrue(result.containsKey("sql"));
        // Data can be absent if SQL execution is skipped or fails; intent call contract is SQL-first.
    }

    @Test
    void callApplication() {
        this.intentAppTest("How many clients?", "get-data");
    }

}
