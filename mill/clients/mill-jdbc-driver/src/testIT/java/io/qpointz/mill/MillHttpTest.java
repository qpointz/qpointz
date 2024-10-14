package io.qpointz.mill;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log
public class MillHttpTest extends BaseTest {

    @Test
    void tryExecute() throws ClassNotFoundException, SQLException {
        Class.forName("io.qpointz.mill.Driver");
        val url = "jdbc:mill:https://bckfuncchangefuncjdb-app.azurewebsites.net/api/";
        log.info(String.format("Url:%s", url));
        val connection = DriverManager.getConnection(url);
        assertNotNull(connection);
        val statement = connection.createStatement();
        assertNotNull(statement);
        val rs = statement.executeQuery("select * from `ts`.`TEST`");
        var rowId=0;
        while (rs.next()) {
            val int1 = rs.getObject(1);
            rowId++;
        }
        assertTrue(rowId>0);
    }

}
