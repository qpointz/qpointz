package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.data.backend.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubstraitSqlParserProviderTest extends BaseTest {

    private SqlProvider.PlanParseResult parse(String sql) {
        return this.getContextRunner().getSqlProvider().parseSql(sql);
    }

    @Test
    void vanilaParse() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

    @Test
    void withExpression() {
        val result = parse("select * from `airlines`.`cities` WHERE `id` <> '0'");
        assertTrue(result.isSuccess(), result.getMessage());
    }

    @Test
    void aggregateAndNamesPreservedInPlan() {
        val result = parse("select count(`c`.`id`) as `ID_CNT`, `c`.`id` as `MY_ID`, `c`.`id` from `airlines`.`cities` as `c` group by `id`");
        assertTrue(result.isSuccess());
        val plan = result.getPlan();
        assertNotNull(plan);
    }

    @Test
    void parseExpression() {
        val result = this.getContextRunner().getSqlProvider()
                .parseSqlExpression(List.of("airlines", "cities"), "`id` <> '0'");
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

}
