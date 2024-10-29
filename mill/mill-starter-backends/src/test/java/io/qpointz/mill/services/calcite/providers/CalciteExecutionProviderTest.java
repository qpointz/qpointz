package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.services.calcite.BaseTest;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.SqlProvider;
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
    CalciteContextFactory ctxFactory;

    @Autowired
    PlanConverter planConverter;


    @Test
    void executeDirectly() throws SQLException {
        val sql = "SELECT * FROM `airlines`.`cities`";
        try (val ctx = ctxFactory.createContext()) {
            val conn = ctx.getCalciteConnection();
            val stmt = conn.prepareStatement(sql);
            val rs = stmt.executeQuery();
            var i = 0;
            while (rs.next()) {
                i++;
            }
            assertTrue(i > 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void relBuilderTest() {
        try (val ctx = ctxFactory.createContext()) {
            val relBuilder = RelBuilder.create(ctx.getFrameworkConfig());
            val node = relBuilder.scan("airlines", "cities").build();
            val stmt = ctx.getRelRunner().prepareStatement(node);
            stmt.executeQuery();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void execTest() {
        val ep = new CalciteExecutionProvider(this.getCtxFactory(), planConverter);
        val pr = sqlProvider.parseSql("SELECT * FROM `airlines`.`cities`");
        assertTrue(pr.isSuccess());
        val res = ep.execute(pr.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
        while (res.hasNext()) {
            val r = res.next();
            log.debug(r.toString());
        }
    }

    @Test
    void execAggregateTest() {
        val ep = new CalciteExecutionProvider(this.getCtxFactory(), planConverter);
        val pr = sqlProvider.parseSql("SELECT `id` as `city_id`, COUNT(*) as `cnt` FROM `airlines`.`cities` GROUP BY `id`");
        assertTrue(pr.isSuccess());
        val res = ep.execute(pr.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
        val fields = res.schema().getFieldsList();
        assertEquals("city_id", fields.get(0).getName());
        assertEquals("cnt", fields.get(1).getName());
    }

    @Test
    void createConnection() throws Exception {
        val ctx = this.getCtxFactory().createContext();
        val stmt = ctx.getCalciteConnection().createStatement();
        val rs = stmt.executeQuery("SELECT `id`, `state` FROM `airlines`.`cities`");
        while (rs.next()) {
            log.info("{} {}",rs.getObject(1),rs.getObject(2));
        }
    }
}