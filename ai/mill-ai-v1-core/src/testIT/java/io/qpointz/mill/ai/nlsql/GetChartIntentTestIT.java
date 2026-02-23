package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {GetChartIntentTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class GetChartIntentTestIT extends BaseIntentTestIT {

    public GetChartIntentTestIT(@Autowired ChatModel chatModel,
                                @Autowired MetadataProvider metadataProvider,
                                @Autowired SqlDialect sqlDialect,
                                @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }

    private Map<String, Object> getChart(String query) {
        val rc = this.getReasoner()
                .reason(ChatUserRequests.query(query))
                .reasoningResponse();

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-chart", rc.intent());

        val ec = intentSpecs()
                .getChartIntent().getCall(rc)
                .asMap();
        log.info("SQL: ({}) => {}", query, ec.getOrDefault("sql", "NULL"));
        assertTrue(ec.containsKey("data"));

        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        assertTrue(ec.containsKey("query-name"));
        assertTrue(ec.containsKey("explanation"));
        assertTrue(ec.containsKey("chart"));
        return ec;
    }


    @ParameterizedTest
    @MethodSource("questionPairs")
    void testChart(String query, String chartType) {
        val ec = getChart(query);
        val chart = ((Map)ec.get("chart"));
        assertTrue(chart.containsKey("type"));
        assertEquals(chartType, chart.get("type"));
        assertTrue(chart.containsKey("config"));
        log.info("{}", chart);
    }

    static Stream<Arguments> questionPairs() {
        return Stream.of(
                Arguments.of("Count clients by country and present as bar chart", "bar") ,
                Arguments.of("LME stocks by client popularity as pie chart", "pie") ,
                Arguments.of("Count stocks traded by clients by exchange and sector and show as treemap", "treemap")
        );
    }

    @Test
    void callApplication() {
        this.intentAppTest("Count clients by country and display as bar chart",
                "get-chart");
    }



}
