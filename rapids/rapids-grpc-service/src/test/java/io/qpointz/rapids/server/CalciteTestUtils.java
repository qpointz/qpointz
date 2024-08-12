package io.qpointz.rapids.server;

import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.*;
import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.RX_PATTERN_KEY;

public class CalciteTestUtils {

    @SneakyThrows
    public static CalciteCtx ctx() {
        Class.forName("org.apache.calcite.jdbc.Driver");
        final var properties = new Properties();
        properties.put("quoting" , "BACK_TICK");
        properties.put("caseSensitive" , true);
        final var connection = DriverManager.getConnection("jdbc:calcite:", properties);
        final var calcite = connection.unwrap(CalciteConnection.class);
        final var rootSchema = calcite.getRootSchema();

        rootSchema.add("airlines", createSchema(rootSchema, "airlines"));
        // Create a Calcite planner

        // Parse and validate the SQL query
        SqlParser.Config parserConfig = SqlParser.Config.DEFAULT
                .withCaseSensitive(false)
                .withQuoting(Quoting.BACK_TICK);

        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .parserConfig(parserConfig)
                .build();

        return new CalciteCtx(connection, config);

    }

     private static Schema createSchema(SchemaPlus parentSchema, String name) {
        final var factory = new RapidsParquetSchemaFactory();
        final var operand = Map.<String,Object>of(
                FS_TYPE, "local",
                DIR_KEY, "../../etc/data/datasets/airlines/parquet",
                RX_DATASET_GROUP_KEY, "dataset",
                RX_PATTERN_KEY, ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)"
        );
        final var schema = (RapidsParquetSchema)factory.create(parentSchema, name, operand);
        //parentSchema.add(name, schema);
        return schema;
    }

    public static class CalciteCtx {
        @Getter
        private final Connection connection;

        @Getter
        private final FrameworkConfig config;


        public CalciteCtx(Connection connection, FrameworkConfig config) {
            this.connection = connection;
            this.config = config;
        }

        public Planner getPlanner() {
            return Frameworks.getPlanner(this.getConfig());
        }

        public RelRoot plan(String sql) throws SqlParseException, ValidationException, RelConversionException {
            final var planner = this.getPlanner();
            SqlNode sqlNode = planner.parse(sql);
            SqlNode validatedSqlNode = planner.validate(sqlNode);
            return planner.rel(validatedSqlNode);
            // Execute the query and get the result as a RelNode
        }

        public io.qpointz.rapids.grpc.Schema schema(String sql) throws ValidationException, SqlParseException, RelConversionException {
            return SchemaBuilder.build(plan(sql).rel.getRowType());
        }

        public java.sql.ResultSet execQuery(String sql) throws ValidationException, SqlParseException, RelConversionException, SQLException {
            var prepared = RelRunners.run(plan(sql).rel);
            return prepared.executeQuery();
        }
    }
}
