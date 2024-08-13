package io.qpointz.mill.service.calcite;

import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersConfiguration;
import io.qpointz.mill.service.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
class SubstraitSqlParserProviderTest extends BaseTest {

    @Autowired
    CalciteContextFactory ctxFactory;

    private SqlProvider.ParseResult parse(String sql) {
        return CalciteServiceProvidersConfiguration
                .sqlParserProvider(ctxFactory)
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