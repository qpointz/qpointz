package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.*;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;

import static io.qpointz.rapids.server.ResponseStatuses.*;

public class CalciteDataService extends AbstractRapidsDataService {

    private final CalciteConnection connection;

    private SchemaPlus rootSchema() {
        if (this.connection == null) {
            return null;
        }
        return this.connection.getRootSchema();
    }

    private RelDataTypeFactory dataTypeFactory() {
        if (this.connection == null) {
            return  null;
        }
        return this.connection.getTypeFactory();
    }

    public CalciteDataService(CalciteConnection connection) {
        this.connection = connection;
    }

    private static ResponseStatus MISSING_ROOT_SCHEMA
            = statusError("Root schema doesn't exists");

    @Override
    protected ListCatalogResponse onListCatalogs(ListCatalogRequest listCatalogRequest) {
        final var response = ListCatalogResponse.newBuilder();
        try {
            final var mayBeRoot = rootSchema();
            if (mayBeRoot == null) {
                return response
                        .setStatus(MISSING_ROOT_SCHEMA)
                        .build();
            }
            mayBeRoot.getSubSchemaNames().stream()
                    .forEach(response::addCatalogues);
            return response
                    .setStatus(statusOk())
                    .build();

        } catch (Exception ex) {
            return response
                    .setStatus(statusError(ex))
                    .build();
        }
    }

    @Override
    protected GetCatalogResponse onGetCatalog(GetCatalogRequest getCatalogRequest) {
        final var response = GetCatalogResponse.newBuilder();
        try {
            final var mayBeRoot = rootSchema();
            if (mayBeRoot == null) {
                return response
                        .setStatus(MISSING_ROOT_SCHEMA)
                        .build();
            }

            final var schema = mayBeRoot.getSubSchema(getCatalogRequest.getCatalogName());
            if (schema == null) {
                return response
                        .setStatus(statusInvalidRequest("Catalog '%s' doesn't exists"))
                        .build();
            }

            final var dataTypeFactory = this.dataTypeFactory();
            for (var tableName : schema.getTableNames()) {
                final var table = this.createTable(tableName, schema.getTable(tableName), dataTypeFactory);
                response.addTables(table);
            }

            return response
                    .setStatus(statusOk())
                    .build();


        } catch (Exception ex) {
            return response
                    .setStatus(statusError(ex))
                    .build();
        }
    }

    private io.qpointz.rapids.grpc.Table createTable(String tableName, Table table, RelDataTypeFactory typeFactory) {
        final var schema = Schema.newBuilder();
        final var type = table.getRowType(typeFactory);
        for(final var field: type.getFieldList()) {
            final var tableField = Field.newBuilder()
                    .setName(field.getName())
                    .setIndex(field.getIndex())
                    .setNullable(field.getType().isNullable())
                    .build();
            schema.addFields(tableField);
        }
        return io.qpointz.rapids.grpc.Table.newBuilder()
                .setName(tableName)
                .setSchema(schema)
                .build();

    }
}