package io.qpointz.delta.calcite.providers;

import io.qpointz.delta.proto.Field;
import io.qpointz.delta.proto.Table;
import io.qpointz.delta.service.MetadataProvider;
import io.substrait.extension.ExtensionCollector;
import io.substrait.isthmus.TypeConverter;
import io.substrait.type.proto.TypeProtoConverter;
import lombok.val;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import java.util.ArrayList;

public class CalciteMetadataProvider implements MetadataProvider {

    private final SchemaPlus rootSchema;
    private final RelDataTypeFactory typeFactory;
    private final TypeProtoConverter typeProtoConverter;

    public CalciteMetadataProvider(SchemaPlus schema, RelDataTypeFactory typeFactory) {
        this.rootSchema = schema;
        this.typeFactory = typeFactory;
        this.typeProtoConverter = new TypeProtoConverter(new ExtensionCollector());
    }

    @Override
    public Iterable<String> getSchemaNames() {
        return this.rootSchema.getSubSchemaNames();
    }

    @Override
    public boolean isSchemaExists(String schemaName) {
        return isRootSchema(schemaName) || this.rootSchema.getSubSchemaNames().contains(schemaName);
    }

    private boolean isRootSchema(String schemaName) {
        return schemaName == null;
    }

    @Override
    public io.qpointz.delta.proto.Schema getSchema(String schemaName) {
        val schema = isRootSchema(schemaName)
                ? this.rootSchema
                : this.rootSchema.getSubSchema(schemaName);

        return io.qpointz.delta.proto.Schema.newBuilder()
                .addAllTables(this.getTables(schemaName, schema))
                .build();
    }

    private Iterable<Table> getTables(String schemaName, Schema schema) {
        val res = new ArrayList<Table>();
        for (var tableName : schema.getTableNames()) {
            val table = schema.getTable(tableName);
            res.add(Table.newBuilder()
                        .setSchemaName(schemaName)
                        .setName(tableName)
                        .addAllFields(this.getFields(table))
                        .build());
        }
        return res;
    }

    private Iterable<Field> getFields(org.apache.calcite.schema.Table table) {
        val res = new ArrayList<Field>();
        val rowType = table.getRowType(this.typeFactory);
        for (var tableField : rowType.getFieldList()) {
            val stype = TypeConverter.DEFAULT
                    .toSubstrait(tableField.getType())
                    .accept(this.typeProtoConverter);
            val field = Field.newBuilder()
                    .setName(tableField.getName())
                    .setIndex(tableField.getIndex())
                    .setType(stype)
                    .build();
            res.add(field);
        }
        return res;
    }

}
