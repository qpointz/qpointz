package io.qpointz.delta.calcite;

import io.qpointz.delta.service.SqlParserProvider;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SubstraitSqlParserProviderTest {

    @Autowired
    CalciteConnection connection;

    private SqlParserProvider.ParseResult parse(String sql) {
        return CalciteDeltaServiceCtx
                .sqlParserProvider(connection)
                .parse(sql);
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