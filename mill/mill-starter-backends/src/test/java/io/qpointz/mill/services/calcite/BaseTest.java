package io.qpointz.mill.services.calcite;


import io.qpointz.mill.proto.MillServiceGrpc;
import io.qpointz.mill.services.*;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.proto.HandshakeRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {
        CalciteServiceConfiguration.class,
        MillGrpcService.class,
        GrpcAdviceAutoConfiguration.class
    }
)
@ActiveProfiles("test-calcite")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseTest {

    @Autowired
    @Getter
    protected CalciteContextFactory ctxFactory;

    @GrpcClient("test-service-calcite")
    protected MillServiceGrpc.MillServiceBlockingStub blockingStub;

    @Test
    void checkConnection() throws Exception {
        val repl = blockingStub.handshake(HandshakeRequest.getDefaultInstance());
        log.info(repl.toString());
    }


}
