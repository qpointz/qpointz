package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.configuration.CalciteProvidersConfiguration;
import io.qpointz.delta.service.SqlProvider;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
class SubstraitSqlParserProviderTest extends BaseTest {

    @Autowired
    CalciteConnection connection;

    private SqlProvider.ParseResult parse(String sql) {
        return CalciteProvidersConfiguration
                .sqlParserProvider(connection)
                .parseSql(sql);
    }

    @Disabled
    @Test
    public void vanilaParse() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

    @Disabled
    @Test
    public void params() {
        val result = parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

}