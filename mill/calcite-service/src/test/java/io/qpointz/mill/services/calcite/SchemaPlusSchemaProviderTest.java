package io.qpointz.mill.services.calcite;

import io.qpointz.mill.services.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SchemaPlusSchemaProviderTest extends BaseTest {

    @Autowired
    MetadataProvider schemaProvider;

    @Autowired
    CalciteContextFactory contextFactory;

    @Test
    void test() {
        assertNotNull(schemaProvider);
    }


}