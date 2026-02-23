package io.qpointz.mill.metadata.impl;

import io.qpointz.mill.metadata.impl.file.FileAnnotationsRepository;
import io.qpointz.mill.metadata.impl.file.FileRepository;
import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataProviderImplTest {

    private static final JdbcBackendContextRunner runner = JdbcBackendContextRunner.jdbcH2Context(
            "jdbc:h2:mem:meta-prov;INIT=RUNSCRIPT FROM '../../test/datasets/cmart/sql/cmart.sql'",
            "cmart");

    private static FileAnnotationsRepository loadAnnotations() throws Exception {
        return new FileAnnotationsRepository(
                FileRepository.from(new FileInputStream("../../test/datasets/cmart/cmart-meta.yaml")));
    }

    @Test
    void trivia() {
        runner.run(ctx -> {
            try {
                val mp = new MetadataProviderImpl(ctx.getSchemaProvider(), loadAnnotations(), null);
                val schema = mp.getSchemas().stream()
                        .filter(k -> k.name().compareToIgnoreCase("cmart") == 0)
                        .findFirst()
                        .get();
                assertTrue(schema.description().isPresent());

                val table = mp.getTables(schema.name()).stream().toList().get(0);
                assertTrue(table.description().isPresent());

                val attribute = table.attributes().stream().toList().get(0);
                assertTrue(attribute.description().isPresent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void metadataSchemaIgnored() {
        runner.run(ctx -> {
            try {
                val mp = new MetadataProviderImpl(ctx.getSchemaProvider(), loadAnnotations(), null);
                val metaschema = mp.getSchemas().stream()
                        .filter(k -> k.name().compareToIgnoreCase("metadata") == 0)
                        .findFirst();
                assertFalse(metaschema.isPresent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void notTablesReturnedForMeta() {
        runner.run(ctx -> {
            try {
                val mp = new MetadataProviderImpl(ctx.getSchemaProvider(), loadAnnotations(), null);
                val metaTables = mp.getTables("metadata");
                assertTrue(metaTables.isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
