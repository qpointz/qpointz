package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.jdbc.BaseTest;
import io.qpointz.mill.services.jdbc.JdbcCalciteConfiguration;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@EnableAutoConfiguration
@ActiveProfiles("test-jdbc")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JdbcExecutionProviderTest extends BaseTest {

    @Autowired
    JdbcContextFactory jdbcContextFactory;

    @Mock
    PlanConverter planConverter;

    @Test
    void trivial() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM TEST", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.hasNext());
        val b = r.next();
        assertTrue(b.getVectorSize()>0);
        assertTrue(b.getSchema().getFieldsList().stream().toList().size()>1);
    }

    @Test
    void executeEmptyRecordset() throws SQLException {
        reset(planConverter);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM TEST WHERE 1=2", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.schema()!=null);
    }



}