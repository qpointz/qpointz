package io.qpointz.mill.services;

import io.qpointz.mill.proto.DataConnectServiceGrpc;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.plan.ImmutablePlan;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MillGrpcServiceRewriteTest extends MillGrpcServiceBaseTest {


    @Test
    @Disabled("Irrelevant test. Subject to review")
    void execPlanTest(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub,
                      @Autowired ExecutionProvider execProvider,
                      @Autowired PlanRewriteChain chain) throws ClassNotFoundException {
        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1");
        val iter = new ResultSetVectorBlockIterator(rs, 10);
        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);

        val rewriter = mock(PlanRewriter.class);
        when(chain.getRewriters()).thenReturn(List.of(rewriter));
        when(rewriter.rewritePlan(any(),any())).thenReturn(ImmutablePlan.builder().build());

        stub
          .execQuery(QueryRequest.newBuilder()
                  .setPlan(io.substrait.proto.Plan.newBuilder().build())
                  .build());

        verify(chain).getRewriters();
    }

}