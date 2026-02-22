package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.SIZED;
import static org.junit.jupiter.api.Assertions.*;

class PlanHelperTest {

    private static final JdbcBackendContextRunner runner = JdbcBackendContextRunner.jdbcH2Context(
            "jdbc:h2:mem:plan-helper;INIT=RUNSCRIPT FROM '../../test/datasets/cmart/sql/cmart.sql'",
            "cmart");

    private static final SimpleExtension.ExtensionCollection extensions = SimpleExtension.loadDefaults();

    @Test
    void trivialNamesScan() {
        runner.run(ctx -> {
            val ph = new PlanHelper(ctx.getSchemaProvider(), extensions);
            assertDoesNotThrow(() -> ph.createNamedScan("cmart", "CLIENT"));
        });
    }

    @Test
    void namedScan() {
        runner.run(ctx -> {
            val ph = new PlanHelper(ctx.getSchemaProvider(), extensions);

            val table = ctx.getSchemaProvider().getSchema("cmart").getTablesList().stream()
                    .filter(k -> k.getName().equals("CLIENT"))
                    .findFirst().get();

            val scan = ph.createNamedScan("cmart", "CLIENT");

            assertEquals(table.getFieldsList().size(), scan.getInitialSchema().names().size());
        });
    }

    @Test
    void executePlan() {
        runner.run(ctx -> {
            val ph = new PlanHelper(ctx.getSchemaProvider(), extensions);
            val namedScan = ph.createNamedScan("cmart", "CLIENT");
            val plan = ph.createPlan(namedScan);
            val res = ctx.getExecutionProvider().execute(plan, QueryExecutionConfig.newBuilder().setFetchSize(10).build());
            val cnt = StreamSupport.stream(Spliterators.spliteratorUnknownSize(res, SIZED), false).count();
            assertTrue(cnt > 0);
        });
    }

    @Test
    void builder() {
        runner.run(ctx -> {
            val ph = new PlanHelper(ctx.getSchemaProvider(), extensions);
            val namedScan = ph.createNamedScan("cmart", "CLIENT");
            val builder = ph.substraitBuilder();
            val idx = namedScan.getInitialSchema().names().indexOf("CLIENT_ID");
            val idRef = builder.fieldReference(namedScan, idx);
            val filterExp = ph.logicalFn().not(builder.equal(idRef, builder.i32(100100)));
            val filter = builder.filter(r -> filterExp, namedScan);
            val plan = ph.createPlan(filter);
            val res = ctx.getExecutionProvider().execute(plan, QueryExecutionConfig.newBuilder().setFetchSize(10).build());
            assertTrue(res.hasNext());
        });
    }
}
