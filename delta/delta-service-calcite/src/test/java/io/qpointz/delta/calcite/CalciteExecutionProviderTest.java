package io.qpointz.delta.calcite;

import io.qpointz.delta.proto.PreparedStatement;
import io.qpointz.delta.proto.SQLStatement;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
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

    @Test
    void createConnection() throws ClassNotFoundException, SQLException {
        val stmt = this.getConnection().createStatement();
        val rs = stmt.executeQuery("SELECT * FROM `kyc`.`CLIENT`");
        while (rs.next()) {
            log.info("{} {}",rs.getObject(1),rs.getObject(2));
        }
    }


}