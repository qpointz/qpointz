package io.qpointz.delta.calcite;

import io.qpointz.delta.service.MetadataProvider;
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
    CalciteConnection connection;

    @Test
    void test() {
        assertNotNull(schemaProvider);
    }


}