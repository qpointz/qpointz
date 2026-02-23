package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = {SchemaMessageSpecTest.class, DefaultServiceConfiguration.class})
@ComponentScan("io.qpointz")
@ActiveProfiles("test-moneta-slim")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WebAppConfiguration
@ComponentScan("io.qpointz.mill")
@EnableAutoConfiguration
class SchemaMessageSpecTest {

    @Autowired
    MetadataService metadataService;

    @Test
    void trivia() throws IOException {
        val sp = SchemaMessageSpec.builder(MessageType.USER, metadataService)
                .includeRelations(true)
                .includeAttributes(true)
                .build();
        val content = sp.getText();
        log.info(content);
        assertTrue(content.toUpperCase().contains("CLIENT_ID"));
        assertTrue(content.toUpperCase().contains("MONETA"));
        assertTrue(content.toUpperCase().contains("MONETA.LOANS.LOAN_ID"));
    }

    @Test
    void filterTable() throws IOException {
        val sp = SchemaMessageSpec.builder(MessageType.USER, metadataService)
                .includeRelations(true)
                .includeAttributes(true)
                .requiredTables(List.of(
                        new ReasoningResponse.IntentTable("MONETA", "CLIENTS", false),
                        new ReasoningResponse.IntentTable("MONETA", "ACCOUNTS", false)
                ))
                .build();
        val content = sp.getText();
        log.info(content);
        assertFalse(content.toUpperCase().contains("MONETA.LOANS"));
        assertFalse(content.toUpperCase().contains("MONETA.LOANS.LOAN_ID"));
    }
}
