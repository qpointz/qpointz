/*
package io.qpointz.delta.sql;

import io.qpointz.delta.proto.Field;
import io.substrait.proto.Type;
import lombok.val;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetMetadataToSubstraitTest extends BaseJdbcTest {

    private Connection conn = createDb("rsmeta", "/sql/simple.sql");

    private ResultSetMetadataToSubstrait testMeta() throws SQLException {
        val rs = execute(conn, "SELECT * FROM TEST");
        return new ResultSetMetadataToSubstrait(rs.getMetaData());
    }

    private Field testField(int idx) throws SQLException {
        return testMeta().asField(idx);
    }

    @Test
    void fieldsList() throws SQLException {
        val rs = execute(conn, "SELECT * FROM TEST");
        val c = new ResultSetMetadataToSubstrait(rs.getMetaData());
        val fields = c.asFields();
        assertEquals(fields.size(), rs.getMetaData().getColumnCount());
    }


    @Test
    void fieldConvert() throws SQLException {
        val c = testMeta();
        val field = c.asField(1);
        assertNotNull(field);
    }

    @Test
    void notNullableField() throws SQLException {
        val n = testField(1).getType().getI32().getNullability();
        assertNotEquals(n, Type.Nullability.NULLABILITY_NULLABLE);
    }

    @Test
    void varcharField() throws SQLException {
        val f = testField(2).getType().getVarchar();
        assertEquals(50, f.getLength());

    }

    @Test
    void charField() throws SQLException {
        val f = testField(3).getType().getFixedChar();
        assertEquals(50, f.getLength());

    }





}*/
