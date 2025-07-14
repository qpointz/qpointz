package io.qpointz.mill.services.metadata.impl;

import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.metadata.impl.file.FileAnnotationsRepository;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {MetadataProviderImplTest.class, DefaultServiceConfiguration.class})
@ComponentScan("io.qpointz")
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WebAppConfiguration
@EnableAutoConfiguration
@Slf4j
class MetadataProviderImplTest {

    @Autowired
    SchemaProvider schemaProvider;

    @Value("file:../test/datasets/cmart/cmart-meta.yaml")
    Resource metaResource;

    @Test
    void trivia() throws IOException {
        val mp = new MetadataProviderImpl(schemaProvider,
                new FileAnnotationsRepository(FileRepository.from(metaResource)),
               null);
        val schema = mp.getSchemas().stream()
                .filter(k-> k.name().compareToIgnoreCase("cmart")==0)
                .findFirst()
                .get();
        assertTrue(schema.description().isPresent());

        val table = mp.getTables(schema.name()).stream().toList().get(0);
        assertTrue(table.description().isPresent());

        val attribute = table.attributes().stream().toList().get(0);
        assertTrue(attribute.description().isPresent());
    }

    @Test
    void metadataSchemaIgnored() throws IOException {
        val mp = new MetadataProviderImpl(schemaProvider,
                new FileAnnotationsRepository(FileRepository.from(metaResource)),
                null);
        val metaschema = mp.getSchemas().stream().filter(k -> k.name().compareToIgnoreCase("metadata")==0).findFirst();
        assertFalse(metaschema.isPresent());
    }

    @Test
    void notTablesReturnedForMeta() throws IOException {
        val mp = new MetadataProviderImpl(schemaProvider,
                new FileAnnotationsRepository(FileRepository.from(metaResource)),
                null);
        val metaTables = mp.getTables("metadata");
        assertTrue(metaTables.isEmpty());
    }

}