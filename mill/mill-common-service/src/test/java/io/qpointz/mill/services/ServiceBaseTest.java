package io.qpointz.mill.services;

import io.qpointz.mill.proto.ExecSqlRequest;
import io.qpointz.mill.proto.MillServiceGrpc;
import io.qpointz.mill.proto.ParseSqlRequest;
import io.qpointz.mill.proto.SQLStatement;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
@ContextConfiguration(classes = {ServiceBaseTestConfiguration.class})
public abstract class ServiceBaseTest {


    protected static ExecSqlRequest.Builder sqlExecuteRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        val request = ExecSqlRequest.newBuilder()
                .setStatement(statement);
        return request;
    }

    protected static ParseSqlRequest.Builder sqlParseRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        val request = ParseSqlRequest.newBuilder()
                .setStatement(statement);
        return request;
    }

    @BeforeEach
    public void resetMocks(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider, @Autowired ExecutionProvider executionProvider) {
        reset(sqlProvider, executionProvider, metadataProvider);
    }

    @Test
    public void testContext(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider,
                            @Autowired ExecutionProvider executionProvider) {
        assertNotNull(sqlProvider);
        assertNotNull(metadataProvider);
        assertNotNull(executionProvider);
    }


}
