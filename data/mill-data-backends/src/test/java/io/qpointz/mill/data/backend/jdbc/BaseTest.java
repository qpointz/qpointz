package io.qpointz.mill.data.backend.jdbc;

import io.qpointz.mill.test.data.backend.BackendContextRunner;
import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseTest {

    @Getter(AccessLevel.PROTECTED)
    private final BackendContextRunner contextRunner = JdbcBackendContextRunner
            .jdbcH2Context("jdbc:h2:mem:test2;INIT=RUNSCRIPT FROM './config/test/testdata.sql'",
                    "ts",
                    null,
                    null,
                    false);


    @Test
    void basicCheck() {
        assertNotNull(getContextRunner().getExecutionProvider());
        assertNotNull(getContextRunner().getSchemaProvider());
    }

}