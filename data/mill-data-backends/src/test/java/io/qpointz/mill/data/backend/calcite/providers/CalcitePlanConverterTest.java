package io.qpointz.mill.data.backend.calcite.providers;

import io.qpointz.mill.data.backend.calcite.BaseTest;
import lombok.val;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.tools.RelRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CalcitePlanConverterTest extends BaseTest {

    @Test
    void trivialConvert() {
        this.getContextRunner().run(ctx -> {
            val pr = ctx.getSqlProvider().parseSql("SELECT `id` as `NewId` FROM `airlines`.`cities`");
            val plan = pr.getPlan();
            val relNode = ctx.getPlanConverter().toRelNode(plan);
            assertNotNull(relNode);
        });
    }

    @Test
    void projectionName() {
        this.getContextRunner().run(ctx -> {
            val pr = ctx.getSqlProvider().parseSql(
                    "SELECT c.`id` AS `city_id`, p.`id` as `passenger_id`, c.*, p.`domicile_city_id` as `passenger_city_id` " +
                    "FROM `airlines`.`cities` c inner join `airlines`.`passenger` p ON c.`id` = p.`domicile_city_id`");
            val plan = pr.getPlan();
            val converted = ctx.getPlanConverter().toRelNode(plan);
            val relNode = converted.node();
            val field = converted.names();
            assertEquals("city_id", field.get(0));
            assertEquals("passenger_id", field.get(1));
            assertEquals("id", field.get(2));
            assertEquals("passenger_city_id", field.get(field.size() - 1));
            assertNotNull(relNode);
        });
    }

    @Test
    void aggregateProjectionName() {
        this.getContextRunner().run(ctx -> {
            val pr = ctx.getSqlProvider().parseSql(
                    "SELECT COUNT(*) as `cnt` FROM `airlines`.`cities` c inner join `airlines`.`passenger` p ON c.`id` = p.`domicile_city_id`");
            val plan = pr.getPlan();
            val converted = ctx.getPlanConverter().toRelNode(plan);
            val relNode = converted.node();
            val field = converted.names();
            assertEquals("cnt", field.get(0));
            assertNotNull(relNode);
        });
    }

    @Test
    void sortProjectionName() {
        this.getContextRunner().run(ctx -> {
            val pr = ctx.getSqlProvider().parseSql("SELECT `state` FROM `airlines`.`cities` order by `state`");
            val plan = pr.getPlan();
            val converted = ctx.getPlanConverter().toRelNode(plan);
            val relNode = converted.node();
            val field = converted.names();
            val collation = relNode.getTraitSet().getTrait(RelCollationTraitDef.INSTANCE);
            assertEquals("state", field.get(0));
            assertNotNull(relNode);
            assertNotNull(collation);
        });
    }

    @Test
    void sortAppliedAtExecution() {
        this.getContextRunner().run(ctx -> {
            val pr = ctx.getSqlProvider().parseSql("SELECT `state` FROM `airlines`.`cities` order by `state`");
            val plan = pr.getPlan();
            val relNode = ctx.getPlanConverter().toRelNode(plan);
            try {
                val con = ctx.getCalciteContextFactory().createContext().getCalciteConnection();
                val runner = con.unwrap(RelRunner.class);
                val stmt = runner.prepareStatement(relNode.node());
                val rs = stmt.executeQuery();
                val getList = new ArrayList<>();
                while (rs.next()) {
                    getList.add(rs.getString(1));
                }
                val sorted = getList.stream().sorted().toList();
                assertEquals(sorted, getList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
