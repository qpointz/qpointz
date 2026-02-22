package io.qpointz.mill.data.backend.jdbc;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.test.data.backend.BackendContextRunner;
import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MultiSchemaBackendTests {

    private final BackendContextRunner contextRunner = JdbcBackendContextRunner
            .jdbcH2Context("jdbc:h2:mem:multi;INIT=RUNSCRIPT FROM './config/test/multi-schema.sql'",
                    "ts",
                    null,
                    null,
                    true);

    @Mock
    PlanConverter planConverter;

    @Test
    void schemasProvider() {
        contextRunner.run(ctx -> {
            val schemas = StreamSupport.stream(ctx.getSchemaProvider().getSchemaNames().spliterator(), false)
                    .collect(Collectors.toSet());
            assertTrue(schemas.contains("SCHEMA1"));
            assertTrue(schemas.contains("SCHEMA2"));
        });
    }

    @Test
    void executeCrossSchema() throws SQLException {
        val query = """
                         SELECT c.`NAME` AS customer, p.`NAME` AS product, o.`AMOUNT`, o.`ORDER_DATE`
                         FROM `SCHEMA1`.`CUSTOMERS` c  JOIN `SCHEMA1`.`ORDERS` o  ON o.`CUSTOMER_ID` = c.`CUSTOMER_ID`
                         JOIN `SCHEMA2`.`PRODUCTS` p ON p.`PRODUCT_ID`  = o.`PRODUCT_ID`
                         ORDER BY o.`ORDER_DATE`;
                     """;

        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);

        contextRunner.withPlanConverter(planConverter).run(ctx -> {
            val r = ctx.getExecutionProvider().execute(ImmutablePlan.builder().build(),
                    QueryExecutionConfig.newBuilder().setFetchSize(10).build());
            assertTrue(r.hasNext());
            val b = r.next();
            assertTrue(b.getVectorSize() > 0);
            assertTrue(b.getSchema().getFieldsList().stream().toList().size() > 1);
        });
    }

}
