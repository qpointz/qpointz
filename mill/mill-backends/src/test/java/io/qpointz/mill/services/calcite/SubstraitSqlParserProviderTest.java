package io.qpointz.mill.services.calcite;

import io.qpointz.mill.services.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
class SubstraitSqlParserProviderTest extends BaseTest {

    @Autowired
    CalciteContextFactory ctxFactory;

    @Autowired
    SqlProvider sqlProvider;

    private SqlProvider.ParseResult parse(String sql) {
        return sqlProvider.parseSql(sql);
    }

    @Test
    public void vanilaParse() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

    @Test
    public void params() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

}