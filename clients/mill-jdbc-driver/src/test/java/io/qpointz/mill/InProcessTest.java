package io.qpointz.mill;

import io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration;
import io.qpointz.mill.client.MillClient;
import io.qpointz.mill.client.MillClientConfiguration;
import io.qpointz.mill.proto.GetSchemaRequest;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.data.backend.MillGrpcService;
import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {
        CalciteBackendAutoConfiguration.class,
        MillGrpcService.class,
        GrpcAdviceAutoConfiguration.class,
        DefaultServiceConfiguration.class
}
)
@ActiveProfiles("in-proc-test")
@Slf4j
public abstract class InProcessTest {

    @Value("${grpc.server.in-process-name}")
    private String serverName;

    protected String connectionUrl() {
        return String.format("jdbc:mill:mem://%s", serverName);
    }

    protected MillClientConfiguration createConfig() {
        return MillClientConfiguration
                .builder(this.connectionUrl())
                .build();
    }

    protected MillConnection createConnection() {
        return new MillConnection(this.createConfig());
    }

    protected MillClient createClient() {
        return MillClient.fromConfig(this.createConfig());
    }

    @Test
    void handshake() {
        val client = createClient();
        assertDoesNotThrow(()-> client.handshake(HandshakeRequest.getDefaultInstance()));
    }

    @Test
    void airlinesSchemaExists() throws MillCodeException {
        val client = createClient();
        val schema = client.getSchema(GetSchemaRequest.newBuilder().setSchemaName("airlines").build());
        assertTrue(schema.getSchema().getTablesList().size()>0);
    }

}
