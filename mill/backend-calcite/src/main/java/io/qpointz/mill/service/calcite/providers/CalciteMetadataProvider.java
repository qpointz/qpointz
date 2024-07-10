package io.qpointz.mill.service.calcite.providers;

import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.Table;
import io.qpointz.mill.service.MetadataProvider;
import io.substrait.extension.ExtensionCollector;
import io.substrait.isthmus.TypeConverter;
import io.substrait.type.proto.TypeProtoConverter;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.schema.Schema;

import java.util.ArrayList;
import java.util.Set;

public class CalciteMetadataProvider implements MetadataProvider {

    @Getter
    private final CalciteContext calciteContext;

    @Getter
    private final TypeProtoConverter typeProtoConverter;

    public CalciteMetadataProvider(CalciteContext calciteContext, ExtensionCollector extensionCollector) {
        this.calciteContext = calciteContext;
        this.typeProtoConverter = new TypeProtoConverter(extensionCollector);
    }

    @Override
    public Set<String> getSchemaNames() {
        return this.getCalciteContext().getRootSchema()
                .getSubSchemaNames();
    }

    @Override
    public boolean isSchemaExists(String schemaName) {
        return isRootSchema(schemaName) || this.getSchemaNames().contains(schemaName);
    }

    private boolean isRootSchema(String schemaName) {
        return schemaName == null;
    }

    @Override
    public io.qpointz.mill.proto.Schema getSchema(String schemaName) {
        val schema = isRootSchema(schemaName)
                ? this.getCalciteContext().getRootSchema()
                : this.getCalciteContext().getRootSchema().getSubSchema(schemaName);

        return io.qpointz.mill.proto.Schema.newBuilder()
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
        val rowType = table.getRowType(this.getCalciteContext().getTypeFactory());
        for (var tableField : rowType.getFieldList()) {
            val substraitType = TypeConverter.DEFAULT
                    .toSubstrait(tableField.getType())
                    .accept(this.getTypeProtoConverter());
            val field = Field.newBuilder()
                    .setName(tableField.getName())
                    .setFieldIdx(tableField.getIndex())
                    .setType(substraitType)
                    .build();
            res.add(field);
        }
        return res;
    }

}
