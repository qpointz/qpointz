package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.configuration.CalciteDataServiceConfiguration;
import io.qpointz.delta.calcite.configuration.CalciteProvidersConfiguration;
import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.proto.SQLStatement;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CalciteExecutionProviderTest extends BaseTest {


    @Test
    @Disabled
    public void execTest() throws SQLException {
        val con = this.getConnection();
        val ep = new CalciteExecutionProvider(con);
        val ps = SQLStatement.newBuilder()
                .setSql("SELECT * FROM `kyc`.`CLIENT`")
                .build();
        throw new RuntimeException("lala");
//        val res = ep.execute(ps, QueryExecutionConfig.newBuilder().setBatchSize(100).build());
//        assertNotNull(res);
    }

    @Test
    @Disabled
    void createConnection() throws ClassNotFoundException, SQLException {
        throw new RuntimeException("lala");
//        val stmt = this.getConnection().createStatement();
//        val rs = stmt.executeQuery("SELECT * FROM `kyc`.`CLIENT`");
//        while (rs.next()) {
//            log.info("{} {}",rs.getObject(1),rs.getObject(2));
//        }
    }


}