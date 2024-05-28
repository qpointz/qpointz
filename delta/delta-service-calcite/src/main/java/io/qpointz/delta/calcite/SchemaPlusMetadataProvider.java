package io.qpointz.delta.calcite;

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
import java.util.Optional;

public class SchemaPlusMetadataProvider implements MetadataProvider {

    private final SchemaPlus schema;
    private final RelDataTypeFactory typeFactory;
    private final TypeProtoConverter typeProtoConverter;

    public SchemaPlusMetadataProvider(SchemaPlus schema, RelDataTypeFactory typeFactory) {
        this.schema = schema;
        this.typeFactory = typeFactory;
        this.typeProtoConverter = new TypeProtoConverter(new ExtensionCollector());
    }
    @Override
    public Iterable<String> getSchemaNames() {
        return this.schema.getSubSchemaNames();
    }

    @Override
    public Optional<io.qpointz.delta.proto.Schema> getSchema(String schemaName) {
        if (schemaName != null && ! this.schema.getSubSchemaNames().contains(schemaName)) {
            return Optional.empty();
        }

        val schema = schemaName == null
                ? this.schema
                : this.schema.getSubSchema(schemaName);

        val builder = io.qpointz.delta.proto.Schema.newBuilder();
        builder
           .addAllTables(this.getTables(schemaName, schema))
           .build();

        return Optional.of(builder.build());
    }

    private Iterable<Table> getTables(String schemaName, Schema schema) {
        val res = new ArrayList<Table>();
        for (var calciteTable : schema.getTableNames()) {
            res.add(this.createTable(schemaName, calciteTable, schema.getTable(calciteTable)));
        }
        return res;
    }

    private Table createTable(String schemaName, String tableName, org.apache.calcite.schema.Table table) {
        val builder = Table.newBuilder();
        return builder
                .setSchemaName(schemaName)
                .setName(tableName)
                .addAllFields(this.getFields(table))
                .build();
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
