package io.qpointz.mill.data.backend.calcite.providers;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.data.backend.calcite.BaseTest;
import io.qpointz.mill.types.logical.StringLogical;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CalciteSchemaProviderTest extends BaseTest {

    @Test
    void testListSchemas() {
        this.getContextRunner().run(ctx -> {
            val schemas = ctx.getSchemaProvider().getSchemaNames();
            assertEquals(Set.of("airlines", "metadata", "testdb", "cmart"), schemas);
        });
    }

    @Test
    void getSchemaReturnsTables() {
        this.getContextRunner().run(ctx -> {
            val schema = ctx.getSchemaProvider().getSchema("airlines");
            assertFalse(schema.getTablesList().isEmpty());
        });
    }

    @Test
    void mapsFieldsToDataType() {
        this.getContextRunner().run(ctx -> {
            val schema = ctx.getSchemaProvider().getSchema("airlines");
            assertFalse(schema.getTablesList().isEmpty());
            val table = schema.getTablesList().stream()
                    .filter(k -> k.getName().equals("passenger"))
                    .findFirst().get();
            val fields = table.getFieldsList();
            assertFalse(fields.isEmpty());
            val fld = fields.get(0);
            assertEquals(StringLogical.INSTANCE.getLogicalTypeId(), fld.getType().getType().getTypeId());
            assertEquals(DataType.Nullability.NOT_NULL, fld.getType().getNullability());
        });
    }

}
