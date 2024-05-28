package io.qpointz.delta.service;

import io.qpointz.delta.proto.*;
import io.qpointz.delta.sql.VectorBlockIterators;
import io.substrait.plan.PlanProtoConverter;
import io.substrait.proto.Plan;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class DeltaServiceSqlTest {

    private static ExecSqlRequest.Builder sqlExecuteRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        val request = ExecSqlRequest.newBuilder()
                .setStatement(statement);
        return request;
    }

    private static ParseSqlRequest.Builder sqlParseRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        val request = ParseSqlRequest.newBuilder()
                .setStatement(statement);
        return request;
    }

    @Test
    void parseSqlSqlNotSupportedTest() {
        val deltaService = new DeltaService(null, null, null);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().parseSql(sqlParseRequest("SELECT * FROM A").build());
            assertEquals(ResponseCode.ERROR_NOT_SUPPORTED, resp.getStatus().getCode());
        }
    }


    void parseSqlFails(SqlProvider.ParseResult.ParseResultBuilder resultBuilder, String sql) {
        val sqlProvider = Mockito.mock(SqlProvider.class, Answers.RETURNS_MOCKS);
        val deltaService = new DeltaService(null, null, sqlProvider);
        val parseResult = resultBuilder.build();
        Mockito
                .when(sqlProvider.parseSql(sql))
                .thenReturn(parseResult);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().parseSql(sqlParseRequest(sql).build());
            assertEquals(ResponseCode.OK, resp.getStatus().getCode());
            assertEquals(parseResult.getMessage(), resp.getErrorMessage());
        }
    }

    @Test
    void parseSqlWithExceptionTest() {
        val sql = "SELECT * FROM A";
        val parseResultBuilder = SqlProvider.ParseResult.builder()
                .originalSql(sql)
                .message("non sql")
                .plan(null);
        //fail on success only
        parseSqlFails(parseResultBuilder.success(false), sql);
        //fail on exception only
        parseSqlFails(parseResultBuilder.exception(new RuntimeException("runtime exp")), sql);
        //fail on both
        parseSqlFails(parseResultBuilder.exception(new RuntimeException("runtime exp")).success(false), sql);
    }

    @Test
    void parseSqlTest() {
        val sql = "SELECT * FROM A";
        val sqlProvider = Mockito.mock(SqlProvider.class, Answers.RETURNS_MOCKS);
        val deltaService = new DeltaService(null, null, sqlProvider);

        val relPlan = io.substrait.plan.ImmutablePlan.builder()
                .build();
        val plan = new PlanProtoConverter().toProto(relPlan);

        Mockito
                .when(sqlProvider.parseSql(sql))
                .thenReturn(SqlProvider.ParseResult.builder()
                        .originalSql(sql)
                        .success(true)
                        .plan(plan)
                        .build());
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().parseSql(sqlParseRequest(sql).build());
            assertEquals(ResponseCode.OK, resp.getStatus().getCode());
            assertEquals(plan , resp.getPlan());
        }
    }

    @Test
    void execSqlSqlNotSupportedTest() {
        val deltaService = new DeltaService(null, null, null);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().execSql(sqlExecuteRequest("SELECT * FROM A").build());
            assertTrue(resp.hasNext(),  "Server not replied");
            val on = resp.next();
            assertEquals(ResponseCode.ERROR_NOT_SUPPORTED, on.getStatus().getCode());
        }
    }

    @Test
    void execSqlparseFailToParseTest() {
        val sql = "SELECT * FROM A";
        val sqlProvider = Mockito.mock(SqlProvider.class, Answers.RETURNS_MOCKS);
        Mockito
                .when(sqlProvider.parseSql(sql))
                .thenReturn(SqlProvider.ParseResult.builder()
                        .originalSql(sql)
                        .exception(new RuntimeException("non sql"))
                        .message("non sql")
                        .plan(null)
                        .build());

        val deltaService = new DeltaService(null, null, sqlProvider);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().execSql(sqlExecuteRequest(sql).build());
            assertTrue(resp.hasNext());
            val on = resp.next();
            assertEquals(ResponseCode.ERROR_EXECUTION_FAILED, on.getStatus().getCode());
        }
    }



    @SneakyThrows
    @Test
    public void execSqlTestSimple() {
        String query = "SELECT * FROM A";
        Plan protoPlan = Plan.newBuilder().build();
        val sqlRequest = sqlExecuteRequest(query).build();
        val sqlProvider = Mockito.mock(SqlProvider.class, Answers.RETURNS_MOCKS);
        val execProvider = Mockito.mock(ExecutionProvider.class, Answers.RETURNS_MOCKS);
        val parseResult = SqlProvider.ParseResult.builder()
                        .success(true)
                        .originalSql(query)
                        .plan(protoPlan)
                        .build();
        Mockito.when(sqlProvider.parseSql(query)).thenReturn(parseResult);
        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1");
        val iter = VectorBlockIterators.fromResultSet(rs, 10);

        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);
        val deltaService = new DeltaService(null, execProvider, sqlProvider);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val res = ctx.blocking().execSql(sqlRequest);
            assertTrue(res.hasNext());
            val on = res.next();
            assertEquals(ResponseCode.OK, on.getStatus().getCode());
            assertEquals(4, on.getVector().getVectorSize());
            assertNotNull(on.getVector().getSchema());
        }
    }

    @Test
    public void execPlanTest() throws ClassNotFoundException {
        val db = H2Db.createFromResource("sql-scripts/test.sql");
        val rs = db.query("SELECT * from T1");
        val iter = VectorBlockIterators.fromResultSet(rs, 10);
        val execProvider = mock(ExecutionProvider.class, Answers.RETURNS_MOCKS);
        when(execProvider.execute(any(io.substrait.plan.Plan.class), any(QueryExecutionConfig.class))).thenReturn(iter);

        val deltaService = new DeltaService(null,execProvider, null);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val res = ctx.blocking()
                    .execPlan(ExecPlanRequest.newBuilder()
                                .setPlan(Plan.newBuilder().build())
                                .build());

            assertTrue(res.hasNext());
            val on = res.next();
            assertEquals(ResponseCode.OK, on.getStatus().getCode());
            assertEquals(4, on.getVector().getVectorSize());
            assertNotNull(on.getVector().getSchema());
        }
    }

    @Test
    public void execPlanFailOnPlanConversion() throws IOException {
        val execProvider = mock(ExecutionProvider.class, Answers.RETURNS_MOCKS);
        when(execProvider.protoToPlan(any())).thenThrow(new IOException("IO"));

        val deltaService = new DeltaService(null,execProvider, null);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val on = ctx.blocking().execPlan(ExecPlanRequest.newBuilder().build()).next();
            assertEquals(ResponseCode.ERROR_SERVER_ERROR, on.getStatus().getCode());
            assertEquals("IO", on.getStatus().getMessage());
        }
    }
}
