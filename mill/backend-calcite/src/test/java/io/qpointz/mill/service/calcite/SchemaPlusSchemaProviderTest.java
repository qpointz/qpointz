package io.qpointz.mill.service.calcite;

import io.qpointz.mill.service.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
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