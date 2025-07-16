package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EnrichModelIntentTestIT.class})
@ActiveProfiles("test-moneta-slim-it")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class EnrichModelIntentTestIT extends BaseIntentTestIT {

    public EnrichModelIntentTestIT(@Autowired ChatModel chatModel,
                                   @Autowired MetadataProvider metadataProvider,
                                   @Autowired DataOperationDispatcher dispatcher) {
        super(chatModel, metadataProvider, dispatcher);
    }

    Map<String, Object> enrichModel(String query) {
        val rc = intentSpecs()
                .reasonCall(query)
                .as(ReasoningResponse.class);

        log.info("Reason: ({}) => {}", query, rc);
        assertEquals("enrich-model", rc.intent());

        val ec = intentSpecs()
                .getEnrichModelIntent()
                .getCall(rc)
                .asMap();

        val retaining = JsonUtils.defaultJsonMapper().convertValue(ec.get("reasoning"), ReasoningResponse.class);
        assertEquals(rc, retaining);
        return ec;
    }

    @Test
    void definingConcept () {
        val ec = enrichModel("Premium clients are clients in ULTRA and WEALTH customer segments");
        log.info("{}", ec);
    }

    @Test
    void multiTableConcept () {
        val ec = enrichModel("Enrich model and define concept : Define Active clients - are clients with at least two active non defaulted loans and trading stocks on more than two exchanges");
        log.info("{}", ec);
    }

    @Test
    void multipleenrichment () {
        val ec = enrichModel("-Premium clients are clients in ULTRA and WEALTH customer segments. - first_name in CLIENTS table represents client name field shouldn't be null and have length more than 10 characters");
        log.info("{}", ec);
    }

    @Test
    void relations () {
        val ec = enrichModel("Define relation: Client may have one or more loans . Loans client identified by client_id ");
        log.info("{}", ec);
    }

    @Test
    void callApplication() {
        this.intentAppTest("Define relation: Client may have one or more loans . Loans client identified by client_id ",
                "enrich-model");
    }

    //@Test
    void noWay() {
        val app = new ChatApplication(this.getCallSpecBuilders(),
                this.getMetadataProvider(),
                this.getDispatcher(),
                MessageSelectors.SIMPLE);

        List.of(
                "How many customers?",
                "Count customers by country",
                "Take 10 countries with most clients and present as bar chart",
                "Take 10 countries with most clients and present as pie bar",
                "Enrich model and define concept : Define Active clients - are clients with at least two active non defaulted loans and trading stocks on more than two exchanges",
                "Define: Premium clients are clients in ULTRA and WEALTH customer segments. - first_name in CLIENTS table represents client name field shouldn't be null and have length more than 10 characters",
                "Define relation: Client may have one or more loans . Loans client identified by client_id ",
                "Describe model",
                "Which tables are contains loan information",
                "Which data contains in ACCOUNT tables",
                "What is client_id attribute and in which tables it exists"
        ).stream().forEach(k-> {
            try {
                log.info("Question:{}", k);
                val respone = app.query(k)
                                .asMap();
                log.info("Response:{}", respone);
                val reason = respone.getOrDefault("reasoning", Map.of());
                log.info("Reason:{}", reason);
                ///assertNotNull(reason.intent());
            } catch (Exception ex) {
                log.error("Failed:", ex);
            }
        });

    }

}
