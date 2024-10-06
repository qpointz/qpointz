package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.jdbc.BaseTest;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcExecutionProviderTest extends BaseTest {

   // private final String connectionUrl = "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM './etc/config/test/testdata.sql'";

    @Autowired
    JdbcContextFactory jdbcContextFactory;

    @Mock
    PlanConverter planConverter;

    @Test
    void trivial() throws SQLException {
        reset(planConverter);
        val sql = "SELECT * FROM TEST";
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
        val sql = "SELECT * FROM TEST WHERE 1=2";
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        assertTrue(r.schema()!=null);
    }



}