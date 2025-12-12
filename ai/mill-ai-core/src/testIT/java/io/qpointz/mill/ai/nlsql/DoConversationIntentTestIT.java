package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {DoConversationIntentTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class DoConversationIntentTestIT extends BaseIntentTestIT {

    public DoConversationIntentTestIT(@Autowired ChatModel chatModel,
                                      @Autowired MetadataProvider metadataProvider,
                                      @Autowired SqlDialect sqlDialect,
                                      @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }

    Map<String, Object> doConversation(String query) {
        val response = this.getReasoner()
                .reason(ChatUserRequests.query(query));
        val rc = response
                .reasoningResponse();

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("do-conversation", rc.intent());

        val ec = intentSpecs()
                .getDoConversationIntent()
                .getCall(rc)
                .asMap();

        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        return ec;
    }

    @Test
    void greetingHello() {
        val result = doConversation("Hello");
        log.info("Response: {}", result);
        assertNotNull(result.get("response"));
        assertNotNull(result.get("language"));
        assertTrue(result.get("response").toString().length() > 0);
        assertEquals("en", result.get("language"));
    }

    @Test
    void greetingHi() {
        val result = doConversation("Hi there!");
        log.info("Response: {}", result);
        assertNotNull(result.get("response"));
        assertNotNull(result.get("language"));
        assertTrue(result.get("response").toString().length() > 0);
    }

    @Test
    void gratitudeThankYou() {
        val result = doConversation("Thank you");
        log.info("Response: {}", result);
        assertNotNull(result.get("response"));
        assertNotNull(result.get("language"));
        assertTrue(result.get("response").toString().length() > 0);
    }

    @Test
    void gratitudeThanks() {
        val result = doConversation("Thanks!");
        log.info("Response: {}", result);
        assertNotNull(result.get("response"));
        assertNotNull(result.get("language"));
        assertTrue(result.get("response").toString().length() > 0);
    }

    @Test
    void callApplication() {
        this.intentAppTest("Hello", "do-conversation");
    }

    @Test
    void callApplicationThankYou() {
        this.intentAppTest("Thank you very much", "do-conversation");
    }

}
