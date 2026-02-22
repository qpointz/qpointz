package io.qpointz.mill.data.backend.calcite.providers;

import io.qpointz.mill.data.backend.calcite.BaseTest;
import io.qpointz.mill.proto.QueryExecutionConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.tools.RelBuilder;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CalciteExecutionProviderTest extends BaseTest {

    @Test
    void executeDirectly() throws SQLException {
        val sql = "SELECT * FROM `airlines`.`cities`";
        this.getContextRunner().run(ctx -> {
            val parseSql = ctx.getSqlProvider().parseSql(sql);
            assertTrue(parseSql.isSuccess());
            try {
                val calciteCtx = ctx.getCalciteContextFactory().createContext();
                val conn = calciteCtx.getCalciteConnection();
                val stmt = conn.prepareStatement(sql);
                val rs = stmt.executeQuery();
                assertTrue(rs.next());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void relBuilderTest() {
        this.getContextRunner().run(context -> {
            try (val ctx = context.getCalciteContextFactory().createContext()) {
                val relBuilder = RelBuilder.create(ctx.getFrameworkConfig());
                val node = relBuilder.scan("airlines", "cities").build();
                val stmt = ctx.getRelRunner().prepareStatement(node);
                stmt.executeQuery();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void execTest() {
        this.getContextRunner().run(ctx -> {
            val parseSql = ctx.getSqlProvider().parseSql("SELECT * FROM `airlines`.`cities`");
            assertTrue(parseSql.isSuccess());
            val exec = ctx.getExecutionProvider().execute(parseSql.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
            assertTrue(exec.hasNext());
            val vb = exec.next();
            assertTrue(vb.getVectorSize()>0);
        });
    }

    @Test
    void execAggregateTest() {
        this.getContextRunner().run(ctx -> {
            val parseSql = ctx.getSqlProvider().parseSql("SELECT `id` as `city_id`, COUNT(*) as `cnt` FROM `airlines`.`cities` GROUP BY `id`");
            assertTrue(parseSql.isSuccess());
            val exec = ctx.getExecutionProvider().execute(parseSql.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
            val resultSchema = exec.schema();
            assertEquals("city_id", resultSchema.getFieldsList().get(0).getName());
            assertEquals("cnt", resultSchema.getFieldsList().get(1).getName());
        });
    }


    @Test
    void execAggregateTestWithLimit() {
        this.getContextRunner().run(ctx -> {
            val parseSql = ctx.getSqlProvider().parseSql("SELECT `id` as `city_id`, COUNT(*) as `cnt` FROM `airlines`.`cities` GROUP BY `id` ORDER BY 1 DESC FETCH FIRST 10 ROWS ONLY");
            assertTrue(parseSql.isSuccess());
            val exec = ctx.getExecutionProvider().execute(parseSql.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
            val resultSchema = exec.schema();
            assertEquals("city_id", resultSchema.getFieldsList().get(0).getName());
            assertEquals("cnt", resultSchema.getFieldsList().get(1).getName());
        });
    }


    @Test
    void createConnection() throws Exception {
        this.getContextRunner().run(ctx -> {
            val res = ctx.getSqlProvider().parseSql("SELECT `id`, `state` FROM `airlines`.`cities`");
            val vbi = ctx.getExecutionProvider().execute(res.getPlan(), QueryExecutionConfig.newBuilder().setFetchSize(100).build());
            val vb = vbi.next();
            assertTrue(vb.getVectorSize()>0);
        });
    }
}