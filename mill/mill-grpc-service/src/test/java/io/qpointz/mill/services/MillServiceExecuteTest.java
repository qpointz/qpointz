package io.qpointz.mill.services;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.utils.SubstraitUtils;
import io.qpointz.mill.vectors.sql.ResultSetVectorBlockIterator;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class MillServiceExecuteTest extends MillServiceBaseTest {



    @Test
    void parseSqlSqlNotSupportedTest(@Autowired MetadataProvider metadataProvider,
                                     @Autowired ExecutionProvider executionProvider,
                                     @Autowired SecurityProvider securityProvide) {
        val serviceHander = new ServiceHandler(metadataProvider, executionProvider, null, securityProvide, null);
        val service = new MillService(serviceHander);
        val ex = assertThrows(StatusRuntimeException.class,
                ()-> service.parseSql(ParseSqlRequest.getDefaultInstance(), null));
        assertEquals(Status.Code.UNIMPLEMENTED, ex.getStatus().getCode());
    }


    void parseSqlFails(MillServiceGrpc.MillServiceBlockingStub stub,
                       SqlProvider sqlProvider,
                       SqlProvider.ParseResult parseResult,
                       String sql) {
        reset(sqlProvider);
        when(sqlProvider.parseSql(sql))
                .thenReturn(parseResult);
        val ex = assertThrows(StatusRuntimeException.class, ()->stub.parseSql(sqlParseRequest(sql).build()));
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        assertTrue(ex.getMessage().contains(parseResult.getMessage()));
    }

    @Test
    void parseSqlWithExceptionTest(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                                   @Autowired SqlProvider sqlProvider) {
        val sql = "SELECT * FROM A";
        //fail on success only
        parseSqlFails(stub, sqlProvider, SqlProvider.ParseResult.fail("non sql"), sql);
        //fail on exception only
        parseSqlFails(stub, sqlProvider, SqlProvider.ParseResult.fail(new RuntimeException("runtime exp")), sql);
    }

    @Test
    void parseSqlTest(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                      @Autowired SqlProvider sqlProvider) {
        val sql = "SELECT * FROM A";
        val relPlan = io.substrait.plan.ImmutablePlan.builder()
                .build();
        when(sqlProvider.parseSql(sql))
            .thenReturn(SqlProvider.ParseResult.success(relPlan));
        val resp = stub.parseSql(sqlParseRequest(sql).build());
        assertEquals(SubstraitUtils.planToProto(relPlan) , resp.getPlan());
    }

    @Test
    void execSqlSqlNotSupportedTest(@Autowired MetadataProvider metadataProvider,
                                    @Autowired ExecutionProvider executionProvider,
                                    @Autowired SecurityProvider securityProvide) {
        val serviceHander = new ServiceHandler(metadataProvider, executionProvider, null, securityProvide, null);
        val service = new MillService(serviceHander);
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder().setSql("select * from `A`"))
                .setConfig(QueryExecutionConfig.newBuilder().setFetchSize(10).build())
                .build();
        val ex = assertThrows(StatusRuntimeException.class,
                ()-> service.execQuery(request, null));
        assertEquals(Status.Code.UNIMPLEMENTED, ex.getStatus().getCode());
    }

    @Test
    void execSqlparseFailToParseTest(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                                     @Autowired SqlProvider sqlProvider) {
        val sql = "SELECT * FROM A";
        val th = new RuntimeException("non sql");

        when(sqlProvider.parseSql(sql))
            .thenReturn(SqlProvider.ParseResult.fail(th));

        val ex = assertThrows(StatusRuntimeException.class, ()->
                stub.execQuery(sqlExecuteRequest(sql).build())
                        .hasNext()
        );
        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        assertTrue(ex.getMessage().contains(th.getMessage()));
    }

    @Test
    void execSqlTestSimple(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                                  @Autowired SqlProvider sqlProvider,
                                  @Autowired ExecutionProvider execProvider) throws ClassNotFoundException {
        String query = "SELECT * FROM A";
        Plan plan = ImmutablePlan.builder().build();
        val sqlRequest = sqlExecuteRequest(query).build();
        val parseResult = SqlProvider.ParseResult.success(plan);
        when(sqlProvider.parseSql(query)).thenReturn(parseResult);

        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1");
        val iter = new ResultSetVectorBlockIterator(rs, 10);
        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);
        val res = stub.execQuery(sqlRequest);
        assertTrue(res.hasNext());
        val on = res.next();
        assertEquals(4, on.getVector().getVectorSize());
        assertNotNull(on.getVector().getSchema());
    }

    @Test
    void execPlanTest(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                             @Autowired ExecutionProvider execProvider ) throws ClassNotFoundException {
        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1");
        val iter = new ResultSetVectorBlockIterator(rs, 10);
        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);
        val res = stub
                .execQuery(QueryRequest.newBuilder()
                            .setPlan(io.substrait.proto.Plan.newBuilder().build())
                            .build());
        assertTrue(res.hasNext());
        val on = res.next();
        assertEquals(4, on.getVector().getVectorSize());
        assertNotNull(on.getVector().getSchema());
    }

    @Test
    void emptyResultShouldReturnOneBlockWithSchema(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                      @Autowired ExecutionProvider execProvider ) throws ClassNotFoundException {
        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1 WHERE 1=2");
        val iter = new ResultSetVectorBlockIterator(rs, 10);
        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);
        val res = stub
                .execQuery(QueryRequest.newBuilder()
                        .setPlan(io.substrait.proto.Plan.newBuilder().build())
                        .build());
        assertTrue(res.hasNext());
        val on = res.next();
        assertEquals(0, on.getVector().getVectorSize());
        assertNotNull(on.getVector().getSchema());
        assertFalse(on.getVector().getSchema().getAllFields().isEmpty());

    }

}
