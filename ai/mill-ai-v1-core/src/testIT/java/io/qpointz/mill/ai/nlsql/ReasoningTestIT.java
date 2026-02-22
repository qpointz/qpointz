package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ReasoningTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class ReasoningTestIT extends BaseIntentTestIT {

    protected ReasoningTestIT(@Autowired ChatModel model,
                              @Autowired MetadataProvider metadataProvider,
                              @Autowired SqlDialect sqlDialect,
                              @Autowired DataOperationDispatcher dispatcher) {
        super(model, metadataProvider, sqlDialect, dispatcher);
    }


    private ReasoningResponse reason(String query) {
        return this.getReasoner()
                .reason(ChatUserRequests.query(query))
                .reasoningResponse();
    }

    @Test
    void trivialReasonRequest() {
        String query = "how many clients?";
        val reason = reason(query);
        log.info("Reason:{}", reason.toString());
        assertNotNull(reason);
        assertTrue(reason.intent().equals("get-data"));
        assertNotNull(reason.language());
    }

    @Test
    void languageDetection() {
        val reason = reason("wie viel Kunden?");
        assertNotNull(reason);
        Locale loc = new Locale(reason.language());
        assertTrue(reason.intent().equals("get-data"));
        assertTrue(reason.requiredTables().size()==1);
        val tbl = reason.requiredTables().get(0);
        assertEquals("CLIENTS", tbl.name());
        assertNotNull(loc);
        assertEquals("de", loc.toLanguageTag());
    }

    @Test
    void unknownIntent() {
        val r = reason("Why snail has blue fins");
        assertEquals("unsupported", r.intent());
    }


}
