package io.qpointz.rapids.server;

import io.qpointz.rapids.grpc.*;
import io.qpointz.rapids.server.vectors.VectorBlockBatchedIterator;
import io.qpointz.rapids.server.vectors.VectorBlockReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.qpointz.rapids.grpc.ResponseStatuses.*;

public class CalciteDataService extends AbstractRapidsDataService {

    private final CalciteDataServiceConfig config;
    private SqlParser.Config parserConfig() {
        return this.config.getParserConfig();
    }

    private CalcitePrepare.Context createPrepareContext () {
        return this.config.getConnection().createPrepareContext();
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
        final var ctx = this.createPrepareContext();
        try {
            final var mayBeRoot = ctx.getDataContext().getRootSchema();
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
        final var ctx = this.createPrepareContext();
        try {
            final var mayBeRoot = ctx.getDataContext().getRootSchema();
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

            final var dataTypeFactory = ctx.getTypeFactory();
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
    protected ExecQueryStreamResult onExecQueryStream(ExecQueryStreamRequest request) {
        return execSqlStreamed(
                request.getSqlRequest().getSql(),
                request.getBatchSize()
        );
    }


    @Builder
    @AllArgsConstructor
    static class CalcitePreparedStatement {
        @Getter
        Schema schema;

        @Getter
        PreparedStatement preparedStatment;
    }

    private CalcitePreparedStatement prepareStatement(ExecQueryRequest request) throws SqlParseException, ValidationException, RelConversionException, SQLException {
        final var preparedStatement =  CalcitePreparedStatement.builder();
        final var ctx = this.createPrepareContext();
        final var rootSchema = ctx.getDataContext().getRootSchema();
        final var config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .parserConfig(this.parserConfig())
                .build();

        final var planner = Frameworks.getPlanner(config);

        final var sqlNode = planner.parse(request.getSql());
        final var validatedSqlNode = planner.validate(sqlNode);
        final var relNode = planner.rel(validatedSqlNode);

        final var rel = relNode.rel;
        final var schema = SchemaBuilder.build(rel.getRowType());
        final var prepStmt = ctx.getRelRunner().prepareStatement(rel);
        return preparedStatement
                .schema(schema)
                .preparedStatment(prepStmt)
                .build();
    }

    protected ExecQueryStreamResult execSqlStreamed(String sql, int batchSize) {
        final var resultBuilder = ExecQueryStreamResult.builder();
        try {
            final var ctx = this.createPrepareContext();
            final var rootSchema = ctx.getDataContext().getRootSchema();
            final var config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .parserConfig(this.parserConfig())
                .build();

            final var planner = Frameworks.getPlanner(config);

            final var sqlNode = planner.parse(sql);
            final var validatedSqlNode = planner.validate(sqlNode);
            final var relNode = planner.rel(validatedSqlNode);

            final var rel = relNode.rel;
            final var schema = SchemaBuilder.build(rel.getRowType());
            resultBuilder.schema(schema);

            final var prepared = ctx.getRelRunner().prepareStatement(rel);
            final var recordSet = prepared.executeQuery();

            final var vectorBlocks = new VectorBlockBatchedIterator(schema, recordSet, batchSize);
            resultBuilder
                    .vectorBlocks(vectorBlocks)
                    .status(ResponseStatuses.statusOk());

        } catch (SqlParseException | SQLException | RelConversionException | ValidationException e) {
            resultBuilder.status(ResponseStatuses.statusError(e));
        }

        return resultBuilder.build();
    }

    @Override
    protected ExecQueryResponse onExecQuery(ExecQueryRequest execQueryRequest) {
        final var resultBuilder = ExecQueryResponse.newBuilder();
        try {
            final var prepared = this.prepareStatement(execQueryRequest);
            resultBuilder
                    .setVector(VectorBlockReader.fromStatement(prepared.getSchema(), prepared.getPreparedStatment()))
                    .setSchema(prepared.getSchema())
                    .setStatus(ResponseStatuses.statusOk());
        } catch (SqlParseException | ValidationException | RelConversionException | SQLException e) {
            resultBuilder.setStatus(ResponseStatuses.statusError(e));
        }
        return resultBuilder.build();
    }
}