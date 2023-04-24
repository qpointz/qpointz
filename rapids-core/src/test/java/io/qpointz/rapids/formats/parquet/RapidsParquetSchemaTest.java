package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.filesystem.LocalFileSystemAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RapidsParquetSchemaTest {

    private static RapidsParquetSchema airlinesRapidsSchema() {
        var rootDir = Path.of(System.getProperty("user.dir") + "/../etc/testModels/formats/parquet/airlines/");
        var fileSystemAdapter = new LocalFileSystemAdapter(rootDir);
        return new RapidsParquetSchema(
                fileSystemAdapter,
                ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)",
                "dataset");
    }

    private static RapidsParquetSchema partitionedSchema() {
        var rootDir = Path.of(System.getProperty("user.dir") + "/../etc/testModels/formats/parquet/partitioned/");
        var fileSystemAdapter = new LocalFileSystemAdapter(rootDir);
        return new RapidsParquetSchema(
                fileSystemAdapter,
                ".*(\\/(?<dataset>[^\\/]+)-(?<year>\\d{4})-(?<month>\\d{2})-\\d{2}\\.parquet$)",
                "dataset");
    }

    @Test
    void readTables() {
        RapidsParquetSchema schema = airlinesRapidsSchema();

        var tableNames = schema.getTableNames();
        assertEquals(4, tableNames.size());
    }


    @Test
    void getTables() {
        var tables = airlinesRapidsSchema().getTableMap();
        assertTrue(tables.containsKey("cities"));
        assertTrue(tables.containsKey("flights"));
        assertTrue(tables.containsKey("passenger"));
        assertTrue(tables.containsKey("segments"));
    }

    @Test
    void getTableFiles() {
        var schema = airlinesRapidsSchema();
        var files = schema.getTablePaths("flights").stream().toList();
        assertEquals(1, files.size());
        var file0 = files.get(0).toFile();
        assertEquals("flights.parquet", file0.getName());
    }

    @Test
    void getTablesPartitioned() {
        var schema = partitionedSchema();
        var files = schema.getTablePaths("person").stream().toList();
        assertTrue(files.size()>1);
    }

    @Test
    void openNonExistentPathThrows() {
        var fs = FileSystems.getDefault();
        var rootDir = Path.of("/../etc/testModels/formats/parquet/doesntexists/");
        var fileSystemAdapter = new LocalFileSystemAdapter(rootDir);
        var schema =  new RapidsParquetSchema(
                fileSystemAdapter,
                ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)",
                "dataset");
        assertThrows(RuntimeException.class, schema::getTableMap);
    }

    private CalciteConnection createAirlinesSchemaConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");
        var prop = new Properties();
        prop.put("quoting", "BACK_TICK");
        var con = DriverManager.getConnection("jdbc:calcite:", prop).unwrap(CalciteConnection.class);
        var typeFactory = con.getTypeFactory();
        var sc = airlinesRapidsSchema();
        con.getRootSchema().add("airlines", sc);
        return con;
    }

    @Test
    void tableMustReturnSchema() throws SQLException, ClassNotFoundException {
        var con = createAirlinesSchemaConnection();
        var typeFactory = con.getTypeFactory();
        var sc = con.getRootSchema().getSubSchema("airlines");
        var cities = sc.getTable("cities");
        var rowType = cities.getRowType(typeFactory);
        assertNotNull(rowType);
    }

    @Test
    void queryTable() throws SQLException, ClassNotFoundException {
        var con = createAirlinesSchemaConnection();
        var stmt = con.prepareStatement("SELECT COUNT(*) FROM `airlines`.`cities`");
        var rs = stmt.executeQuery();
        assertTrue(rs.next());
        var cnt = rs.getLong(1);
        assertTrue(cnt>1);
    }

}