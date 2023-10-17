package io.qpointz.rapids.server;

import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory;
import io.qpointz.rapids.grpc.GetCatalogRequest;
import io.qpointz.rapids.grpc.GetCatalogResponse;
import io.qpointz.rapids.grpc.ListCatalogRequest;
import lombok.SneakyThrows;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
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
    void testQuery() throws SQLException {
        final var stmt = connection().prepareStatement("SELECT COUNT(*) FROM `airlines`.`segments`");
        final var rs = stmt.executeQuery();
        rs.next();
        assertTrue(rs.getInt(1)>1);
    }

    @Test
    void listSchemas() {
        final var svc = new CalciteDataService(connection());
        var resp = svc.onListCatalogs(ListCatalogRequest.newBuilder().build());
        var set = resp.getCataloguesList().stream().collect(Collectors.toSet());
        assertEquals(
                Set.of("airlines","a1","a2","a3", "metadata"),
                set
        );
    }

    private GetCatalogResponse getCatalog(String catalog) {
        final var svc = new CalciteDataService(connection());
        return svc.onGetCatalog(
                GetCatalogRequest.newBuilder()
                        .setCatalogName(catalog)
                        .build());
    }
    @Test
    void getSchemaTablesList() {
        final var resp = getCatalog("airlines");
        final var tables = resp.getTablesList();
        assertEquals(4, tables.size());
    }


}