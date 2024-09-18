package io.qpointz.mill;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MillPreparedStatementTest extends BaseTest {

    @Test
    public void createConnectionAndStatement() throws ClassNotFoundException, SQLException {
        val con = getConnection();
        val stmt = con.prepareStatement("select * from `airlines`.`cities`");
        val rs = stmt.executeQuery();
        var rowId=0;
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>0);

    }

}