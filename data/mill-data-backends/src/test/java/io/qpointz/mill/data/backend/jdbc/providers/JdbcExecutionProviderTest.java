package io.qpointz.mill.data.backend.jdbc.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.data.backend.jdbc.BaseTest;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcExecutionProviderTest extends BaseTest {

    @Test
    void trivial() throws SQLException {
        val planConverter = mock(PlanConverter.class);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM TEST", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);

        this.getContextRunner()
                .withPlanConverter(planConverter)
                .run(ctx -> {
                    val r = ctx.getExecutionProvider().execute(
                            ImmutablePlan.builder().build(),
                            QueryExecutionConfig.newBuilder().setFetchSize(10).build());
                    assertTrue(r.hasNext());
                    val b = r.next();
                    assertTrue(b.getVectorSize()>0);
                    assertTrue(b.getSchema().getFieldsList().stream().toList().size()>1);

        });
    }

    @Test
    void executeEmptyRecordset() throws SQLException {
        val planConverter = mock(PlanConverter.class);
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM TEST WHERE 1=2", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        //val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        this.getContextRunner()
                .withPlanConverter(planConverter)
                .run(ctx -> {
                    val r = ctx.getExecutionProvider()
                            .execute(ImmutablePlan.builder().build(),
                                    QueryExecutionConfig.newBuilder().setFetchSize(10).build());
                    assertTrue(r.schema()!=null);
                });
    }



}