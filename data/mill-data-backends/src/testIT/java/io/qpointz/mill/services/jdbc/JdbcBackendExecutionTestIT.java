package io.qpointz.mill.services.jdbc;

import io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        JdbcBackendAutoConfiguration.class,
        DefaultServiceConfiguration.class
}
)
@ActiveProfiles("test-moneta-it")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
class JdbcBackendExecutionTestIT {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private ExecutionProvider executionProvider;

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private SchemaProvider schemaProvider;

    @Autowired
    JdbcContextFactory jdbcContextFactory;

    @Mock
    PlanConverter planConverter;

    @Test
    void basicCheck() {
        assertNotNull(executionProvider);
        assertNotNull(schemaProvider);
    }

    @Test
    void schemasProvider() {
        val schemas = StreamSupport.stream(this.getSchemaProvider().getSchemaNames().spliterator(), false)
                        .collect(Collectors.toSet());
        assertTrue(schemas.size() > 0, "Should have at least one schema");
        log.info("Found schemas: {}", schemas);
    }

    @Test
    void executeClientsQuery() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `clients` LIMIT 10", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        assertTrue(b.getSchema().getFieldsList().size() > 0, "Should have schema fields");
        log.info("Clients query returned {} rows with {} fields", b.getVectorSize(), b.getSchema().getFieldsList().size());
    }

    @Test
    void executeAccountsQuery() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `accounts` LIMIT 10", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Accounts query returned {} rows", b.getVectorSize());
    }

    @Test
    void executeTransactionsQuery() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `transactions` LIMIT 10", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Transactions query returned {} rows", b.getVectorSize());
    }

    @Test
    void executeClientsAccountsJoin() throws SQLException {
        reset(planConverter);
        val query = """
                SELECT c.`client_id`, c.`first_name`, c.`last_name`, a.`account_id`, a.`account_type`, a.`balance`
                FROM `clients` c
                JOIN `accounts` a ON a.`client_id` = c.`client_id`
                LIMIT 10
                """;
        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results from join");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        assertTrue(b.getSchema().getFieldsList().size() >= 6, "Should have joined fields");
        log.info("Clients-Accounts join returned {} rows with {} fields", b.getVectorSize(), b.getSchema().getFieldsList().size());
    }

    @Test
    void executeAccountsTransactionsJoin() throws SQLException {
        reset(planConverter);
        val query = """
                SELECT a.`account_id`, a.`account_type`, t.`transaction_id`, t.`transaction_type`, t.`amount`, t.`transaction_date`
                FROM `accounts` a
                JOIN `transactions` t ON t.`account_id` = a.`account_id`
                LIMIT 10
                """;
        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results from join");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Accounts-Transactions join returned {} rows", b.getVectorSize());
    }

    @Test
    void executeAggregationQuery() throws SQLException {
        reset(planConverter);
        val query = """
                SELECT `account_type`, COUNT(*) as account_count, SUM(`balance`) as total_balance
                FROM `accounts`
                GROUP BY `account_type`
                """;
        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have aggregation results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one group");
        log.info("Aggregation query returned {} groups", b.getVectorSize());
    }

    @Test
    void executeFilteredQuery() throws SQLException {
        reset(planConverter);
        val query = """
                SELECT * FROM `clients`
                WHERE `segment` = 'WEALTH'
                LIMIT 10
                """;
        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have filtered results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one matching row");
        log.info("Filtered query returned {} rows", b.getVectorSize());
    }

    @Test
    void executeLoansQuery() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `loans` LIMIT 10", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Loans query returned {} rows", b.getVectorSize());
    }

    @Test
    void executeStocksQuery() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `stocks` LIMIT 10", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Stocks query returned {} rows", b.getVectorSize());
    }

    @Test
    void executeComplexJoin() throws SQLException {
        reset(planConverter);
        val query = """
                SELECT c.`first_name`, c.`last_name`, c.`segment`,
                       a.`account_type`, a.`balance`,
                       t.`transaction_type`, t.`amount`, t.`transaction_date`
                FROM `clients` c
                JOIN `accounts` a ON a.`client_id` = c.`client_id`
                JOIN `transactions` t ON t.`account_id` = a.`account_id`
                WHERE c.`segment` = 'ULTRA'
                LIMIT 10
                """;
        val sql = new PlanConverter.ConvertedPlanSql(query, List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext(), "Should have results from complex join");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        log.info("Complex join returned {} rows", b.getVectorSize());
    }

    @Test
    void executeEmptyRecordset() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `clients` WHERE 1=2", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertNotNull(r.schema(), "Should have schema even for empty result");
        assertTrue(r.hasNext(), "Has empty record block");
        val rb = r.next();
        assertTrue(rb.getVectorSize()==0, "Should not have results");
    }

}

