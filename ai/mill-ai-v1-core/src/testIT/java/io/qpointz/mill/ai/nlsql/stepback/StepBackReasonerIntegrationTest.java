package io.qpointz.mill.ai.nlsql.stepback;

import io.qpointz.mill.ai.BaseIntegrationTestIT;
import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.nlsql.BaseIntentTestIT;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.ai.nlsql.models.stepback.StepBackResponse;
import io.qpointz.mill.ai.nlsql.reasoners.StepBackReasoner;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test exercising the Step-Back reasoner against the Moneta test profile using a real ChatModel.
 * Purposefully light on assertions to accommodate LLM variability while ensuring schema/context wiring works.
 */
@SpringBootTest(classes = {BaseIntegrationTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class StepBackReasonerIntegrationTest extends BaseIntentTestIT {

    public StepBackReasonerIntegrationTest(@Autowired ChatModel chatModel,
                                      @Autowired MetadataProvider metadataProvider,
                                      @Autowired SqlDialect sqlDialect,
                                      @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, sqlDialect, dispatcher);
    }

    private Map<String, Object> stepBack(String query) {
        val reasoner = new StepBackReasoner(this.getCallSpecBuilders(), this.getMetadataProvider(), this.getMessageSelector());
        return reasoner
                .reason(ChatUserRequests.query(query))
                .reply()
                .asMap();
    }

    @Test
    void noClarificationReasoning() {
        String query = "Count clients in South Korea";
        Map<String, Object> raw = stepBack(query);
        ReasoningResponse response = JsonUtils.defaultJsonMapper().convertValue(raw, ReasoningResponse.class);
        assertNotNull(response);
        assertNotNull(response.intent());
        assertEquals("get-data", response.intent());
    }

    @Test
    void clarificationAfterReasoning() {
        String query = "How many premium clients?";
        Map<String, Object> raw = stepBack(query);
        StepBackResponse response = JsonUtils.defaultJsonMapper().convertValue(raw, StepBackResponse.class);
        assertNotNull(response);
        assertEquals(query, response.query());
        assertEquals(response.needClarification(), true);
        assertNotNull(response.stepBack());
    }

    @Test
    void clarificationFlowResolvesPremiumDefinition() {
        String query = "Count premium clients";
        val reasoner = new StepBackReasoner(this.getCallSpecBuilders(), this.getMetadataProvider(), this.getMessageSelector());

        val reply1 = reasoner.reason(ChatUserRequests.query(query));
        val resp1 = reply1
                .reply()
                .as(StepBackResponse.class);

        assertNotNull(resp1);
        assertEquals(query, resp1.query());
        assertTrue(resp1.needClarification());
        assertFalse(resp1.questionsSafe().isEmpty());

        String query2 = "Premium clients are clients in ULTRA and WEALTH segment";
        val reply2 = reasoner.reason(ChatUserRequests.clarify(query2, reply1.reasoningId()));
        val resp2 = reply2
                .reply()
                .as(ReasoningResponse.class);
        assertEquals("get-data", resp2.intent());
    }
}
