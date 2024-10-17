package io.qpointz.mill.azure.functions;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendFunctionsTest {

    @Autowired
    BackendFunctions funcs;

    @Test
    void injected() {
        assertNotNull(funcs);
    }

    @Test
    void executeSimple() {
        val req = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("select * from `ts`.`TEST`")
                        .build())
                .setConfig(QueryExecutionConfig.newBuilder()
                        .setFetchSize(10)
                        .build())
                .build();
        val resp = funcs.submitQuery().apply(req);
        assertNotNull(resp);
        assertTrue(resp.hasPagingId());
        assertTrue(resp.hasVector());
        assertTrue(resp.getVector().hasSchema());
    }

    @Test
    void executeNoRecordsReturnsSchema() {
        val req = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("select * from `ts`.`TEST` WHERE `ID`< 0")
                        .build())
                .setConfig(QueryExecutionConfig.newBuilder()
                        .setFetchSize(10)
                        .build())
                .build();
        val resp = funcs.submitQuery().apply(req);
        assertNotNull(resp);
        assertFalse(resp.hasPagingId());
        assertTrue(resp.hasVector());
        assertTrue(resp.getVector().hasSchema());
    }

}