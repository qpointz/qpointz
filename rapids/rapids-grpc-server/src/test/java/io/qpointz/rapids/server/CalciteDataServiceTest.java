package io.qpointz.rapids.server;

import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory;
import io.qpointz.rapids.grpc.GetCatalogRequest;
import io.qpointz.rapids.grpc.GetCatalogResponse;
import io.qpointz.rapids.grpc.ListCatalogRequest;
import io.qpointz.rapids.grpc.ResponseCode;
import lombok.SneakyThrows;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.*;
import static org.junit.jupiter.api.Assertions.*;


class CalciteDataServiceTest {

    @SneakyThrows
    CalciteConnection connection() {
        Class.forName("org.apache.calcite.jdbc.Driver");
        final var properties = new Properties();
        properties.put("quoting" , "BACK_TICK");
        properties.put("caseSensitive" , true);
        final var connection = DriverManager.getConnection("jdbc:calcite:", properties);
        final var calcite = connection.unwrap(CalciteConnection.class);
        final var root = calcite.getRootSchema();
        createSchema(root, "airlines");
        createSchema(root, "a1");
        createSchema(root, "a2");
        createSchema(root, "a3");
        return calcite;
    }

    private static Schema createSchema(SchemaPlus parentSchema, String name) {
        final var factory = new RapidsParquetSchemaFactory();
        final var operand = Map.<String,Object>of(
                FS_TYPE, "local",
                DIR_KEY, "../../example/data/airlines_parquet",
                RX_DATASET_GROUP_KEY, "dataset",
                RX_PATTERN_KEY, ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)"
        );
        final var schema = (RapidsParquetSchema)factory.create(parentSchema, name, operand);
        parentSchema.add(name, schema);
        return schema;
    }

    @Test
    void noJdbc() throws ClassNotFoundException, SQLException, SqlParseException, ValidationException, RelConversionException {
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
        Planner planner = Frameworks.getPlanner(config);

        // Define the SQL query
        String sql = "SELECT `city` AS `c1`, `id`,`state`,`city` FROM `airlines`.`cities`";

try {

        //SqlParser sqlParser = SqlParser.create(sql, parserConfig);
        SqlNode sqlNode = planner.parse(sql);
        SqlNode validatedSqlNode = planner.validate(sqlNode);



        // Optimize the query
        RelRoot relNode = planner.rel(validatedSqlNode);

        // Execute the query and get the result as a RelNode
        var prepared = RelRunners.run(relNode.rel);


        var schema = SchemaBuilder.build(relNode.rel.getRowType());

        var rs = prepared.executeQuery();

    } catch (SqlParseException e) {
        System.err.println("SQL Parsing error: " + e.getMessage());
        throw e;
    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }

    }


    @Test
    void testQuery() throws SQLException {
        final var stmt = connection().prepareStatement("SELECT COUNT(*) FROM `airlines`.`segments`");
        final var rs = stmt.executeQuery();
        rs.next();
        assertTrue(rs.getInt(1)>1);
    }

    @Test
    void listSchemas() throws SQLException {
        final var svc = CalciteDataService.create(connection());
        var resp = svc.onListCatalogs(ListCatalogRequest.newBuilder().build());
        var set = resp.getCataloguesList().stream().collect(Collectors.toSet());
        assertEquals(
                Set.of("airlines","a1","a2","a3", "metadata"),
                set
        );
    }

    private GetCatalogResponse getCatalog(String catalog) throws SQLException {
        final var svc = CalciteDataService.create(connection());
        return svc.onGetCatalog(
                GetCatalogRequest.newBuilder()
                        .setCatalogName(catalog)
                        .build());
    }
    @Test
    void getSchemaTablesList() throws SQLException {
        final var resp = getCatalog("airlines");
        assertEquals(ResponseCode.OK, resp.getStatus().getCode(), resp.getStatus().getMessage());
        final var tables = resp.getTablesList();
        assertEquals(4, tables.size());
    }


}