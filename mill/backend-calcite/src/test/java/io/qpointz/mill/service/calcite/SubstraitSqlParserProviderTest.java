package io.qpointz.mill.service.calcite;

import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.mill.service.calcite.providers.CalciteContext;
import io.qpointz.mill.service.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
class SubstraitSqlParserProviderTest extends BaseTest {

    @Autowired
    CalciteContext calciteCtx;

    private SqlProvider.ParseResult parse(String sql) {
        return CalciteServiceProvidersContextConfiguration
                .sqlParserProvider(calciteCtx)
                .parseSql(sql);
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