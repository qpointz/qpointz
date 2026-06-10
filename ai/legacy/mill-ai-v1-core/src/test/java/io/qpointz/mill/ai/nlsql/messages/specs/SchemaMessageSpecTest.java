package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.nlsql.metadata.SchemaMessageMetadataPorts;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.metadata.CatalogPath;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.data.metadata.SchemaEntityKinds;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.repository.FacetRepository;
import io.qpointz.mill.metadata.service.MetadataEntityService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(
    classes = { SchemaMessageSpecTest.class, DefaultServiceConfiguration.class },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    }
)
@ComponentScan(
    basePackages = "io.qpointz.mill",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "io\\.qpointz\\.mill\\.metadata\\.api\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "io\\.qpointz\\.mill\\.test\\.security\\..*"
        )
    }
)
@ActiveProfiles("test-moneta-slim")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableAutoConfiguration
@Import(SchemaMessageSpecTest.TestBeans.class)
class SchemaMessageSpecTest {

    @Autowired
    MetadataEntityService metadataEntityService;

    @Autowired
    FacetRepository facetRepository;

    @Autowired
    MetadataEntityUrnCodec urnCodec;

    @Test
    void trivia() throws IOException {
        val schemas = metadataEntityService.findByKind(SchemaEntityKinds.SCHEMA);
        assertFalse(schemas.isEmpty(), "Expected at least one schema in metadata service");
        val attributes = metadataEntityService.findByKind(SchemaEntityKinds.ATTRIBUTE);
        assertFalse(attributes.isEmpty(), "Expected at least one attribute in metadata service");
        val sampleAttr = attributes.get(0);
        CatalogPath path = urnCodec.decode(sampleAttr.getId());
        assertNotNull(path.getSchema());
        assertNotNull(path.getTable());
        assertNotNull(path.getColumn());

        val ports = new SchemaMessageMetadataPorts(metadataEntityService, facetRepository, urnCodec);
        val sp = SchemaMessageSpec.forMetadata(MessageType.USER, ports)
                .includeRelations(true)
                .includeAttributes(true)
                .build();
        val content = sp.getText();
        System.out.println(content);
        val upper = content.toUpperCase();
        assertTrue(upper.contains(path.getSchema().toUpperCase()));
        assertTrue(upper.contains(path.getTable().toUpperCase()));
        assertTrue(upper.contains(path.getColumn().toUpperCase()));
    }

    @Test
    void filterTable() throws IOException {
        val ports = new SchemaMessageMetadataPorts(metadataEntityService, facetRepository, urnCodec);
        val sp = SchemaMessageSpec.forMetadata(MessageType.USER, ports)
                .includeRelations(true)
                .includeAttributes(true)
                .requiredTables(java.util.List.of(
                        new io.qpointz.mill.ai.nlsql.models.ReasoningResponse.IntentTable("MONETA", "CLIENTS", false),
                        new io.qpointz.mill.ai.nlsql.models.ReasoningResponse.IntentTable("MONETA", "ACCOUNTS", false)
                ))
                .build();
        val content = sp.getText();
        log.info(content);
        assertFalse(content.toUpperCase().contains("MONETA.LOANS"));
        assertFalse(content.toUpperCase().contains("MONETA.LOANS.LOAN_ID"));
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        tools.jackson.databind.ObjectMapper objectMapper() {
            return tools.jackson.databind.json.JsonMapper.builder().build();
        }
    }
}
