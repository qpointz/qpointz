package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.azure.AzureFileSystemAdapter;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureADLSParquetSchemaITTest {

    public static String storageAccountName = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_NAME");
    public static String storageAccountKey = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_KEY");
    public static String itContainer = "rapids-it";
    public static String itContainerModels = "rapids-it-models";

    private static RapidsParquetSchema airlinesRapidsSchema() throws IOException {
        var azureFileAdapter = AzureFileSystemAdapter.create(storageAccountName, storageAccountKey,
                                                            itContainerModels,
                                                 "models/formats/parquet/airlines");
        return new RapidsParquetSchema(
                azureFileAdapter,
                ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)",
                "dataset");
    }

    @Test
    void readTables() throws IOException {
        RapidsParquetSchema schema = airlinesRapidsSchema();
        var tableNames = schema.getTableNames();
        assertEquals(4, tableNames.size());
    }

    private CalciteConnection createAirlinesSchemaConnection() throws ClassNotFoundException, SQLException, IOException {
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
    void queryTable() throws SQLException, ClassNotFoundException, IOException {
        var con = createAirlinesSchemaConnection();
        var stmt = con.prepareStatement("SELECT COUNT(*) FROM `airlines`.`cities`");
        var rs = stmt.executeQuery();
        assertTrue(rs.next());
        var cnt = rs.getLong(1);
        assertTrue(cnt>1);
    }

    @Test
    void querySegmentsTable() throws SQLException, ClassNotFoundException, IOException {
        var con = createAirlinesSchemaConnection();
        var stmt = con.prepareStatement("SELECT COUNT(*) FROM `airlines`.`segments`");
        var rs = stmt.executeQuery();
        assertTrue(rs.next());
        var cnt = rs.getLong(1);
        assertTrue(cnt>1);
    }

}
