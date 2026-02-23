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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {RefineIntentTestIt.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class RefineIntentTestIt extends BaseIntentTestIT {

    protected RefineIntentTestIt(@Autowired ChatModel chatModel,
                                 @Autowired MetadataProvider metadataProvider,
                                 @Autowired SqlDialect sqlDialect,
                                 @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }

    private static ChatClient.Builder createChatBuilder(ChatModel model) {
        val chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(200)
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
        return ChatClient.builder(model)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                );
    }

    //original request get-data
    Map<String, Object> getData(String query) {
        val rc = this.getReasoner()
                .reason(ChatUserRequests.query(query))
                .reasoningResponse();

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-data", rc.intent());

        val ec = intentSpecs()
                .getDataIntent().getCall(rc)
                .asMap();
        log.info("SQL: ({}) => {}", query, ec.getOrDefault("sql", "NULL"));
        assertTrue(ec.containsKey("data"));

        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        assertTrue(ec.containsKey("query-name"));
        assertTrue(ec.containsKey("explanation"));

        return ec;
    }

    //original request get-chart
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

    //original request get-chart
    private Map<String, Object> followUp(String query) {
        val rc = this.getReasoner()
                .reason(ChatUserRequests.query(query))
                .reasoningResponse();

        log.info("Reason: ({}) => {}", query, rc);
        //assertEquals("follow-up", rc.intent());

        val ec = intentSpecs()
                .getRefineIntent().getCall(rc)
                .asMap();
        return ec;
    }


    @Test
    void call() {
        var r1 = getData("get clients");
        var r2 = followUp("- drop phone column.");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertTrue(r2.containsKey("refine"));
        var refine = (Map<String, Object>)r2.get("refine");
        assertEquals("get-data", refine.get("original-intent"));

        r1 = r2;
        r2 = followUp("limit to customers in Korea and Singapore");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        refine = (Map<String, Object>)r2.get("refine");
        assertEquals("get-data", refine.get("original-intent"));

        r1 = r2;
        r2 = followUp("Exclude customers in REGULAR SEGMENT");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        refine = (Map<String, Object>)r2.get("refine");
        assertEquals("get-data", refine.get("original-intent"));

        r1 = getChart("count clients by country and present as bar chart");
        r2 = followUp("show only 10 countries with most clients");
        refine = (Map<String, Object>)r2.get("refine");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertEquals("get-chart", refine.get("original-intent"));
    }

    @Test
    void callApplication() {
        this.intentAppTest("exclude phone column from last query",
                "refine");
    }


}
