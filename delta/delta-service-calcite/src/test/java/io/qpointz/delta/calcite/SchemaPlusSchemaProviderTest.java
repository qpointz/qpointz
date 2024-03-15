package io.qpointz.delta.calcite;

import io.qpointz.delta.service.SchemaProvider;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class SchemaPlusSchemaProviderTest extends BaseTest {

    @Autowired
    SchemaPlusSchemaProvider schemaProvider;

    @Autowired
    CalciteConnection connection;

    @Test
    void test() {
        assertNotNull(schemaProvider);
    }


}