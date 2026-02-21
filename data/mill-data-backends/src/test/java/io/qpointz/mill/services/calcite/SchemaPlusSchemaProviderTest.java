package io.qpointz.mill.services.calcite;

import io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration;
import io.qpointz.mill.services.SchemaProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test-calcite")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableAutoConfiguration
class SchemaPlusSchemaProviderTest extends BaseTest {

    @Autowired
    SchemaProvider schemaProvider;

    @Test
    void test() {
        assertNotNull(schemaProvider);
    }


}