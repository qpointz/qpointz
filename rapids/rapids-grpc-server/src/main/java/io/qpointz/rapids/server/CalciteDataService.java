package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.*;
import io.qpointz.rapids.server.vectors.VectorBlockIterator;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;

import java.sql.SQLException;

import static io.qpointz.rapids.server.ResponseStatuses.*;

public class CalciteDataService extends AbstractRapidsDataService {


    private final CalciteDataServiceConfig config;
    private SchemaPlus rootSchema() {
        return this.config.getRootSchema();
    }
    private SqlParser.Config parserConfig() {
        return this.config.getParserConfig();
    }

    private RelDataTypeFactory typeFactory() {
        return this.config.getTypeFactory();
    }

    private FrameworkConfig frameworkConfig() {
        return Frameworks.newConfigBuilder()
                .defaultSchema(this.rootSchema())
                .parserConfig(this.parserConfig())
                .build();
    }

    private Planner planner() {
        return Frameworks.getPlanner(this.frameworkConfig());
    }

    private CalciteDataService(CalciteDataServiceConfig config) {
        this.config = config;
    }

    public static CalciteDataService create(CalciteConnection connection) throws SQLException {
        final var config = CalciteDataServiceConfig.builder()
                .defaultConfig(connection)
                .build();

        return create(config);
    }

    public static CalciteDataService create(CalciteDataServiceConfig config) {
        return new CalciteDataService(config);
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

            final var dataTypeFactory = this.typeFactory();
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
        final var type = table.getRowType(typeFactory);
        final var schema = SchemaBuilder.build(type);
        return io.qpointz.rapids.grpc.Table.newBuilder()
                .setName(tableName)
                .setSchema(schema)
                .build();

    }

    @Override
    protected ExecSqlBatchedResult onExecSqlBatched(ExecSqlRequest request) {
        return execSql(
                request.getSql(),
                request.getBatchSize()
        );
    }

    protected ExecSqlBatchedResult execSql(String sql, int batchSize) {
        final var resultBuilder = ExecSqlBatchedResult.builder();
        try {
            final var planner = this.planner();

            final var sqlNode = planner.parse(sql);
            final var validatedSqlNode = planner.validate(sqlNode);
            final var relNode = planner.rel(validatedSqlNode);

            final var rel = relNode.rel;
            final var schema = SchemaBuilder.build(rel.getRowType());
            resultBuilder.schema(schema);

            final var prepared = config.getRelRunner().prepareStatement(rel);
            final var recordSet = prepared.executeQuery();

            final var vectorBlocks = new VectorBlockIterator(schema, recordSet, batchSize);
            resultBuilder
                    .vectorBlocks(vectorBlocks)
                    .status(ResponseStatuses.statusOk());

        } catch (SqlParseException e) {
            resultBuilder.status(ResponseStatuses.statusError(e));
        } catch (ValidationException e) {
            resultBuilder.status(ResponseStatuses.statusError(e));
        } catch (RelConversionException e) {
            resultBuilder.status(ResponseStatuses.statusError(e));
        } catch (SQLException e) {
            resultBuilder.status(ResponseStatuses.statusError(e));
        }

        return resultBuilder.build();
    }


}