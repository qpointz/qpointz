package io.qpointz.mill.service.calcite;


import io.qpointz.mill.proto.MillServiceGrpc;
import io.qpointz.mill.service.calcite.configuration.CalciteConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceCalciteContextConfiguration;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.service.MillService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = {
        CalciteConfiguration.class,
        CalciteServiceProvidersContextConfiguration.class
} )
@ContextConfiguration(classes = {
        CalciteServiceProvidersContextConfiguration.class,
        CalciteConfiguration.class,
        CalciteServiceCalciteContextConfiguration.class,
        MillService.class,
        GrpcAdviceAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@Slf4j
public class BaseTest {

    @Autowired
    @Getter
    private CalciteConnection connection;

    @GrpcClient("testservice")
    protected MillServiceGrpc.MillServiceBlockingStub blockingStub;

    @Test
    public void checkConnection() throws Exception {
        val repl = blockingStub.handshake(HandshakeRequest.getDefaultInstance());
        log.info(repl.toString());
    }


}
