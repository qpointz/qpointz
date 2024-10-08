package io.qpointz.mill;

import io.qpointz.mill.client.MillClient;
import io.qpointz.mill.client.MillClientConfiguration;
import io.qpointz.mill.proto.GetSchemaRequest;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.services.MillService;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProvidersConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

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
@ActiveProfiles("in-proc-test")
@Slf4j
public abstract class InProcessTest {

    @Value("${grpc.server.in-process-name}")
    private String serverName;

    protected String connectionUrl() {
        return String.format("jdbc:mill:in-proc://%s", serverName);
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
    void airlinesSchemaExists() {
        val client = createClient();
        val schema = client.getSchema(GetSchemaRequest.newBuilder().setSchemaName("airlines").build());
        assertTrue(schema.getSchema().getTablesList().size()>0);
    }

}
