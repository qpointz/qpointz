package io.qpointz.mill.services;

import io.qpointz.mill.proto.ParseSqlRequest;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.configuration.ServiceBaseTestConfiguration;
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
@ContextConfiguration(classes = {ServiceBaseTestConfiguration.class, BackendConfiguration.class, DefaultServiceConfiguration.class})
public abstract class ServiceBaseTest {


    protected static QueryRequest.Builder sqlExecuteRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        return QueryRequest.newBuilder()
                .setStatement(statement);
    }

    protected static ParseSqlRequest.Builder sqlParseRequest(String sql) {
        val statement = SQLStatement.newBuilder()
                .setSql(sql)
                .build();
        return ParseSqlRequest.newBuilder()
                .setStatement(statement);
    }

    @BeforeEach
    public void resetMocks(@Autowired SqlProvider sqlProvider, @Autowired SchemaProvider schemaProvider, @Autowired ExecutionProvider executionProvider) {
        reset(sqlProvider, executionProvider, schemaProvider);
    }

    @Test
    public void testContext(@Autowired SqlProvider sqlProvider, @Autowired SchemaProvider schemaProvider,
                            @Autowired ExecutionProvider executionProvider) {
        assertNotNull(sqlProvider);
        assertNotNull(schemaProvider);
        assertNotNull(executionProvider);
    }


}
