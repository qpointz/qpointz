package io.qpointz.delta.calcite;

import io.qpointz.delta.proto.PreparedStatement;
import io.qpointz.delta.proto.SQLStatement;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CalciteExecutionProviderTest extends BaseTest {


    @Test
    public void execTest() throws SQLException {
        val con = this.getConnection();
        val ep = new CalciteExecutionProvider(con);
        val ps = SQLStatement.newBuilder()
                .setStatement("SELECT * FROM `kyc`.`CLIENT`")
                .build();
        val res = ep.execute(ps, 100);
        assertNotNull(res);
    }


}