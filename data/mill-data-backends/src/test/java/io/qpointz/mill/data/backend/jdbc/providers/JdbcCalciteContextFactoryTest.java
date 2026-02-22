package io.qpointz.mill.data.backend.jdbc.providers;

import io.qpointz.mill.data.backend.jdbc.BaseTest;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JdbcCalciteContextFactoryTest extends BaseTest {

    @Test
    @Disabled
    void validatesJdbcFunctions() {
        this.getContextRunner().run(ctx -> {
            val sql = "SELECT * FROM `ts`.`TEST` WHERE REGEXP_LIKE(`FIRST_NAME`, '.+') = TRUE";
            val result = ctx.getSqlProvider().parseSql(sql);
            assertTrue(result.isSuccess(), result.getMessage());
        });
    }

}