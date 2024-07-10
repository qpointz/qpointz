package io.qpointz.mill.service.calcite;

import io.qpointz.mill.service.calcite.providers.CalciteContext;
import io.qpointz.mill.service.calcite.providers.CalciteExecutionProvider;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.service.SqlProvider;
import io.substrait.extension.ExtensionCollector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.tools.RelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CalciteExecutionProviderTest extends BaseTest {

    @Autowired
    SqlProvider sqlProvider;

    @Autowired
    ExtensionCollector extensionCollector;

    @Autowired
    CalciteContext context;


    @Test
    public void executeDirectly() throws SQLException {
        val sql = "SELECT * FROM `airlines`.`cities`";
        val conn = context.getCalciteConnection();
        val stmt = conn.prepareStatement(sql);
        val rs = stmt.executeQuery();
        var i =0;
        while (rs.next()) {
            i++;
        }
        assertTrue(i>0);
    }

    @Test
    public void relBuilderTest() throws SQLException {
        val relBuilder = RelBuilder.create(this.context.getFrameworkConfig());
        val node = relBuilder.scan("airlines", "cities").build();
        val stmt = this.context.getRelRunner().prepareStatement(node);
        val rs = stmt.executeQuery();
    }

    @Test
    public void execTest() {
        val con = this.getConnection();
        val calciteContext = new CalciteContext(con);
        val ep = new CalciteExecutionProvider(calciteContext);
        val pr = sqlProvider.parseSql("SELECT * FROM `airlines`.`cities`");
        assertTrue(pr.isSuccess());
        val res = ep.execute(pr.getPlan(), QueryExecutionConfig.newBuilder().setBatchSize(100).build());
        while (res.hasNext()) {
            val r = res.next();
            log.debug(r.toString());
        }
    }

    @Test
    void createConnection() throws SQLException {
        val stmt = this.getConnection().createStatement();
        val rs = stmt.executeQuery("SELECT `id`, `state` FROM `airlines`.`cities`");
        while (rs.next()) {
            log.info("{} {}",rs.getObject(1),rs.getObject(2));
        }
    }
}