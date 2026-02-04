package io.qpointz.mill.services.jdbc;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        BackendConfiguration.class,
        JdbcCalciteConfiguration.class,
        DefaultServiceConfiguration.class,
        BackendConfiguration.class
}
)
@ActiveProfiles("test-jdbc-multi-schema")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
public class MultiSchemaBackendTests {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private SchemaProvider schemaProvider;

    @Autowired
    JdbcContextFactory jdbcContextFactory;

    @Mock
    PlanConverter planConverter;

    @Test
    void schemasProvider() {
        val schemas = StreamSupport.stream(this.getSchemaProvider().getSchemaNames().spliterator(), false)
                        .collect(Collectors.toSet());
        assertTrue(schemas.contains("SCHEMA1"));
        assertTrue(schemas.contains("SCHEMA2"));
    }

    @Test
    void executeCrossSchema() throws SQLException {
        reset(planConverter);
        val query = """
                         SELECT c.`NAME` AS customer, p.`NAME` AS product, o.`AMOUNT`, o.`ORDER_DATE`
                         FROM `SCHEMA1`.`CUSTOMERS` c  JOIN `SCHEMA1`.`ORDERS` o  ON o.`CUSTOMER_ID` = c.`CUSTOMER_ID`
                         JOIN `SCHEMA2`.`PRODUCTS` p ON p.`PRODUCT_ID`  = o.`PRODUCT_ID`
                         ORDER BY o.`ORDER_DATE`;
                     """;

        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext());
        val b = r.next();
        assertTrue(b.getVectorSize()>0);
        assertTrue(b.getSchema().getFieldsList().stream().toList().size()>1);
    }

}
