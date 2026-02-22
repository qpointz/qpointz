package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanDispatcherImplTest {

    private static final JdbcBackendContextRunner runner = JdbcBackendContextRunner.jdbcH2Context(
            "jdbc:h2:mem:plan-disp;INIT=RUNSCRIPT FROM '../../test/datasets/cmart/sql/cmart.sql'",
            "cmart");

    @Test
    void trivial() {
        runner.run(ctx -> {
            val dispatcher = new PlanDispatcherImpl(
                    SimpleExtension.loadDefaults(), ctx.getSchemaProvider());
            assertNotNull(dispatcher);
            assertNotNull(dispatcher.plan());
        });
    }
}
