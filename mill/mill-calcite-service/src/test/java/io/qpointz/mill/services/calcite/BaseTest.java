package io.qpointz.mill.services.calcite;


import io.qpointz.mill.proto.MillServiceGrpc;
import io.qpointz.mill.services.*;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProvidersConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.proto.HandshakeRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = {
        CalciteServiceProvidersConfiguration.class
} )
@ContextConfiguration(classes = {
        CalciteServiceProperties.class,
        CalciteServiceConfiguration.class,
        MillService.class,
        GrpcAdviceAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@Slf4j
public class BaseTest {

    @Autowired
    @Getter
    protected CalciteContextFactory ctxFactory;

    @GrpcClient("testservice")
    protected MillServiceGrpc.MillServiceBlockingStub blockingStub;

    @Test
    public void checkConnection() throws Exception {
        val repl = blockingStub.handshake(HandshakeRequest.getDefaultInstance());
        log.info(repl.toString());
    }


}
