package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.providers.CalciteContext;
import io.qpointz.delta.calcite.providers.CalciteExecutionProvider;
import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.proto.SQLStatement;
import io.qpointz.delta.service.SqlProvider;
import io.substrait.extension.ExtensionCollector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.tools.RelBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class CalciteExecutionProviderTest extends BaseTest {

    @Autowired
    SqlProvider sqlProvider;

    @Autowired
    ExtensionCollector extensionCollector;

    @Autowired
    CalciteContext context;

    @Test
    public void relBuilderTest() throws SQLException {
        val relBuilder = RelBuilder.create(this.context.getFrameworkConfig());
        val node = relBuilder.scan("kyc", "CLIENT").build();
        val stmt = this.context.getRelRunner().prepareStatement(node);
        val rs = stmt.executeQuery();
    }

    @Test
    public void execTest() throws SQLException {
        val con = this.getConnection();
        val calciteContext = new CalciteContext(con);
        val ep = new CalciteExecutionProvider(calciteContext);
        val pr = sqlProvider.parseSql("SELECT * FROM `kyc`.`CLIENT`");
        assertTrue(pr.isSuccess());
        val res = ep.execute(pr.getPlan(), QueryExecutionConfig.newBuilder().setBatchSize(100).build());
//        assertNotNull(res);
    }

    @Test
    @Disabled
    void createConnection() throws ClassNotFoundException, SQLException {
        val stmt = this.getConnection().createStatement();
        val rs = stmt.executeQuery("SELECT `ID` FROM `kyc`.`CLIENT`");
        while (rs.next()) {
            log.info("{} {}",rs.getObject(1),rs.getObject(2));
        }
    }


}