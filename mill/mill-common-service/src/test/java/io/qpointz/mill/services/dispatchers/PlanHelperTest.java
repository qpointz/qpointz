package io.qpointz.mill.services.dispatchers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.substrait.expression.Expression;
import io.substrait.extension.SimpleExtension;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.ImmutableRoot;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.SIZED;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {PlanDispatcherImplTest.class, DefaultServiceConfiguration.class})
@ComponentScan("io.qpointz")
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlanHelperTest {

    @Autowired
    MetadataProvider metadataProvider;

    @Autowired
    ExecutionProvider executionProvider;

    @Autowired
    SimpleExtension.ExtensionCollection extensionCollection;

    @Test
    void trivialNamesScan()  {
        val ph = new PlanHelper(metadataProvider, extensionCollection);
        assertDoesNotThrow(()-> ph.createNamedScan("cmart", "CLIENT"));
    }

    @Test
    void namedScan() {
        val ph = new PlanHelper(metadataProvider, extensionCollection);

        val table = metadataProvider.getSchema("cmart").getTablesList().stream()
                .filter(k-> k.getName().equals("CLIENT"))
                .findFirst().get();

        val scan = ph.createNamedScan("cmart", "CLIENT");

        assertEquals(table.getFieldsList().size(), scan.getInitialSchema().names().size());
    }

    @Test
    void executePlan() {
        val ph = new PlanHelper(metadataProvider, extensionCollection);
        val namedScan = ph.createNamedScan("cmart", "CLIENT");
        val plan = ph.createPlan(namedScan);
        val res = executionProvider.execute(plan, QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        val cnt = StreamSupport.stream(Spliterators.spliteratorUnknownSize(res, SIZED), false).count();
        assertTrue(cnt > 0);
    }

    @Test
    void builder() {
        val ph = new PlanHelper(metadataProvider, extensionCollection);
        val namedScan = ph.createNamedScan("cmart", "CLIENT");
        val builder = ph.substraitBuilder();
        val idx = namedScan.getInitialSchema().names().indexOf("CLIENT_ID");
        val idRef = builder.fieldReference(namedScan, idx);
        val filterExp = ph.logicalFn().not(builder.equal(idRef, builder.i32(100100)));
        val filter = builder.filter(r -> filterExp, namedScan);
        val plan = ph.createPlan(filter);
        val res = executionProvider.execute(plan, QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(res.hasNext());
    }


}