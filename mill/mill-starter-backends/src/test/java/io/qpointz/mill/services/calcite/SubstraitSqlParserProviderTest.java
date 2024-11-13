package io.qpointz.mill.services.calcite;

import io.qpointz.mill.services.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
class SubstraitSqlParserProviderTest extends BaseTest {

//    @Autowired
//    CalciteContextFactory ctxFactory;

    @Autowired
    SqlProvider sqlProvider;

    private SqlProvider.PlanParseResult parse(String sql) {
        return sqlProvider.parseSql(sql);
    }

    @Test
    void vanilaParse() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

    @Test
    void withExpression() {
        val result = parse("select * from `airlines`.`cities` WHERE `id` <> '0'");
        assertTrue(result.isSuccess());
    }

    @Test
    void parseExpression() {
        val result = this.sqlProvider.parseSqlExpression(List.of("airlines", "cities"), "`id` <> '0'");
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

}