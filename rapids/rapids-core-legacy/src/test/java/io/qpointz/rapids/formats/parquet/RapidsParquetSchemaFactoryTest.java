package io.qpointz.rapids.formats.parquet;

import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class RapidsParquetSchemaFactoryTest {

    @Test
    void createCalciteConnection() throws ClassNotFoundException, SQLException {
        var prop= new Properties();
        prop.put("model", "src/test/resources/formats/parquet/model.json" );
        Class.forName("org.apache.calcite.jdbc.Driver");
        var con = DriverManager.getConnection("jdbc:calcite:", prop).unwrap(CalciteConnection.class);
        var subSchemas = con.getRootSchema().getSubSchemaNames();
        assertTrue(subSchemas.size()>0);
    }


}