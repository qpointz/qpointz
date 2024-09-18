package io.qpointz.mill.service.calcite.providers;

import io.qpointz.mill.service.calcite.BaseTest;
import io.qpointz.mill.service.calcite.CalciteContextFactory;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialects;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class CalcitePlanConverterTest extends BaseTest {

    @Autowired
    CalciteContextFactory ctxFactory;

    private static final SqlDialect dialect = CalciteSqlDialect.DEFAULT;

    @Test
    void trivialConvert() {
        val sql = new CalciteSqlProvider(ctxFactory);
        val pr = sql.parseSql("SELECT `id` as `NewId` FROM `airlines`.`cities`");
        val plan = pr.getPlan();
        val pc = new CalcitePlanConverter(ctxFactory, dialect);
        val relNode = pc.toRelNode(plan);
        assertNotNull(relNode);
    }

    @Test
    void projectionName() {
        val sql = new CalciteSqlProvider(ctxFactory);
        val pr = sql.parseSql("SELECT c.`id` AS `city_id`, p.`id` as `passenger_id`, c.*, p.`domicile_city_id` as `passenger_city_id` FROM `airlines`.`cities` c inner join `airlines`.`passenger` p ON c.`id` = p.`domicile_city_id`");
        val plan = pr.getPlan();
        val pc = new CalcitePlanConverter(ctxFactory, dialect);
        val relNode = pc.toRelNode(plan);
        val rowType = relNode.getRowType();
        val field = rowType.getFieldNames();
        assertEquals("city_id", field.get(0));
        assertEquals("passenger_id", field.get(1));
        assertEquals("id", field.get(2));
        assertEquals("passenger_city_id", field.get(field.size()-1));
        assertNotNull(relNode);
    }


}