package io.qpointz.mill.data.backend.grpc;

import io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.backend.grpc.config.MillGrpcConfiguration;
import io.qpointz.mill.proto.DataConnectServiceGrpc;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.proto.ProtocolVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = MillGrpcHandshakeIT.Config.class)
@ActiveProfiles("test")
class MillGrpcHandshakeIT {

    @SpringBootConfiguration
    @Import({
            MillGrpcConfiguration.class,
            MillGrpcService.class,
            GrpcExceptionInterceptor.class,
            GrpcServiceDescriptor.class,
            DefaultServiceConfiguration.class,
            MillGrpcTestSupportConfiguration.class,
    })
    @EnableAutoConfiguration(exclude = CalciteBackendAutoConfiguration.class)
    static class Config {
    }

    @Test
    void shouldHandshake(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub) {
        var response = stub.handshake(HandshakeRequest.getDefaultInstance());
        assertEquals(ProtocolVersion.V1_0, response.getVersion());
        assertTrue(response.getCapabilities().getSupportDialect());
        // SqlProvider test double stubs supportsSql() = true (unlike v1 tests that reset mocks before each case)
        assertTrue(response.getCapabilities().getSupportSql());
    }
}
