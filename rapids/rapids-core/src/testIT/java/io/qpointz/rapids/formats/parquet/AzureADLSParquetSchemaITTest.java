package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.azure.AzureFileSystemAdapter;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
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
                "dataset", 300);
    }

    @Test
    void readTables() throws IOException {
        RapidsParquetSchema schema = airlinesRapidsSchema();
        var tableNames = schema.getTableNames();
        Assertions.assertEquals(4, tableNames.size());
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
        Assertions.assertTrue(rs.next());
        var cnt = rs.getLong(1);
        assertTrue(cnt>1);
    }

    @Test
    void querySegmentsTable() throws SQLException, ClassNotFoundException, IOException {
        var con = createAirlinesSchemaConnection();
        var stmt = con.prepareStatement("SELECT COUNT(*) FROM `airlines`.`segments`");
        var rs = stmt.executeQuery();
        Assertions.assertTrue(rs.next());
        var cnt = rs.getLong(1);
        assertTrue(cnt>1);
    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        var exec = new AzureADLSParquetSchemaITTest();
        var con = exec.createAirlinesSchemaConnection();
        Function<String, Object> fn = (sql) -> {
            try {
                var ps = con.prepareStatement(sql);
                var rs = ps.executeQuery();
                rs.next();
                log.debug("Fetched {}", rs.getObject(1));
                return rs.getObject(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        for(int i=0;i<3;i++) {
            System.out.println("Iteration %d".formatted(i));
            log.info("SEGMENTS");
            fn.apply("SELECT * FROM `airlines`.`segments`");

            log.info("CITIES");
            fn.apply("SELECT * FROM `airlines`.`cities`");

        }
    }

}
