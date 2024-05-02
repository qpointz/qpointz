package io.qpointz.delta.calcite;

import io.qpointz.delta.service.ServiceSecurityConfig;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubstraitSqlParserProviderTest {

    @Autowired
    CalciteConnection connection;


    @Test
    public void vanilaParse() {
        val prse = CalciteDeltaServiceCtx.sqlParserProvider(connection);
        val result = prse.parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

    @Test
    public void params() {
        val prse = CalciteDeltaServiceCtx.sqlParserProvider(connection);
        val result = prse.parse("select * from `metadata`.`COLUMNS`");
        assertTrue(result.isSuccess());
    }

}