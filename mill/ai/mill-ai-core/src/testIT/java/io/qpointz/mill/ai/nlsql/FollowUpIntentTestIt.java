package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
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

@SpringBootTest(classes = {FollowUpIntentTestIt.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class FollowUpIntentTestIt  extends BaseIntentTestIT {

    protected FollowUpIntentTestIt(@Autowired ChatModel chatModel,
                                   @Autowired MetadataProvider metadataProvider,
                                   @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, dispatcher);
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
        val rc = callSpecs()
                .reasonSpec(query).call()
                .as(ReasoningResponse.class);

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-data", rc.intent());

        val ec = callSpecs()
                .getDataSpec(rc).call()
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
        val rc = callSpecs()
                .reasonSpec(query).call()
                .as(ReasoningResponse.class);

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("get-chart", rc.intent());

        val ec = callSpecs()
                .getChartSpec(rc).call()
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
        val rc = callSpecs()
                .reasonSpec(query).call()
                .as(ReasoningResponse.class);

        log.info("Reason: ({}) => {}", query, rc);
        //assertEquals("follow-up", rc.intent());

        val ec = callSpecs()
                .followUpSpec(rc).call()
                .asMap();
        return ec;
    }


    @Test
    void call() {
        var r1 = getData("get clients");
        var r2 = followUp("- drop phone column.");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertTrue(r2.containsKey("original-intent"));
        assertEquals("get-data", r2.get("original-intent"));

        r1 = r2;
        r2 = followUp("limit to customers in Korea and Singapore");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertEquals("get-data", r2.get("original-intent"));

        r1 = r2;
        r2 = followUp("Exclude customers in REGULAR SEGMENT");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertEquals("get-data", r2.get("original-intent"));

        r1 = getChart("count clients by country and present as bar chart");
        r2 = followUp("Limit results to 10 countries with most clients");
        assertNotEquals(r1.get("sql"), r2.get("sql"));
        assertEquals("get-chart", r2.get("original-intent"));
    }

    @Test
    void callApplication() {
        this.intentAppTest("exclude phone column from last query",
                "follow-up");
    }


}
