package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.Table;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.RelToDatabaseTypeConverter;
import io.substrait.extension.ExtensionCollector;
import io.substrait.type.proto.TypeProtoConverter;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.schema.Schema;

import java.util.ArrayList;
import java.util.Set;

public class CalciteMetadataProvider implements MetadataProvider {

    @Getter
    private final CalciteContextFactory ctxFactory;

    @Getter
    private final TypeProtoConverter typeProtoConverter;

    public CalciteMetadataProvider(CalciteContextFactory calciteContextFactory, ExtensionCollector extensionCollector) {
        this.ctxFactory = calciteContextFactory;
        this.typeProtoConverter = new TypeProtoConverter(extensionCollector);
    }

    @Override
    public Set<String> getSchemaNames() {
        try (
                val ctx = ctxFactory.createContext()
        ) {
            return ctx.getRootSchema()
                    .getSubSchemaNames();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try (val ctx = this.ctxFactory.createContext()) {
            val schema = isRootSchema(schemaName)
                    ? ctx.getRootSchema()
                    : ctx.getRootSchema().getSubSchema(schemaName);

            return this.calciteToMillSchema(schema, schemaName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected io.qpointz.mill.proto.Schema calciteToMillSchema(Schema schema , String schemaName) {
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
        try (val ctx = this.ctxFactory.createContext()) {
            val rowType = table.getRowType(ctx.getTypeFactory());
            for (var tableField : rowType.getFieldList()) {
                DataType dataType = RelToDatabaseTypeConverter.DEFAULT
                        .convert(tableField.getType())
                        .asDataType();
                val field = Field.newBuilder()
                        .setName(tableField.getName())
                        .setFieldIdx(tableField.getIndex())
                        .setType(dataType)
                        .build();
                res.add(field);
            }
            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
