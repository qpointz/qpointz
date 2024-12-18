package io.qpointz.mill.services;

import io.qpointz.mill.proto.DataConnectServiceGrpc;
import io.qpointz.mill.proto.ParseSqlRequest;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MillGrpcServiceMetadataTest.class,
        MillGrpcService.class,
        DefaultServiceConfiguration.class,
        MillServiceBaseTestConfiguration.class})
public abstract class MillGrpcServiceBaseTest {


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
    public void resetMocks(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider, @Autowired ExecutionProvider executionProvider) {
        reset(sqlProvider, executionProvider, metadataProvider);
    }

    @Test
    public void testContext(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider,
                            @Autowired ExecutionProvider executionProvider, @Autowired MillGrpcService millGrpcService,
                            @Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub blocking, @Autowired PasswordEncoder passwordEncoder) {
        assertNotNull(sqlProvider);
        assertNotNull(metadataProvider);
        assertNotNull(executionProvider);
        assertNotNull(millGrpcService);
        assertNotNull(blocking);
        assertNotNull(passwordEncoder);
    }


}
