package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.services.calcite.BaseTest;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.types.logical.StringLogical;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CalciteSchemaProviderTest extends BaseTest {

    @Autowired
    ExtensionCollector extensionCollector;

    @Autowired
    CalciteContextFactory ctxFactory;

    @Test
    void testListSchemas() {
        val mp = new CalciteSchemaProvider(this.ctxFactory, this.extensionCollector);
        val schemas = mp.getSchemaNames();
        assertEquals(Set.of("airlines", "metadata", "testdb", "cmart"), schemas);
    }

    @Test
    void getSchemaReturnsTables() {
        val mp = new CalciteSchemaProvider(this.ctxFactory, this.extensionCollector);
        val schema = mp.getSchema("airlines");
        assertFalse(schema.getTablesList().isEmpty());
    }

    @Test
    void mapsFieldsToDataType() {
        val mp = new CalciteSchemaProvider(this.ctxFactory, this.extensionCollector);
        val schema = mp.getSchema("airlines");
        assertFalse(schema.getTablesList().isEmpty());
        val table = schema.getTablesList().stream().filter(k-> k.getName().equals("passenger")).findFirst().get();
        val fields = table.getFieldsList();
        assertFalse(fields.isEmpty());
        val fld = fields.get(0);
        assertEquals(StringLogical.INSTANCE.getLogicalTypeId(), fld.getType().getType().getTypeId());
        assertEquals(DataType.Nullability.NOT_NULL, fld.getType().getNullability());

    }

}