package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.TestITProfile;
import io.qpointz.mill.proto.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static io.qpointz.mill.TestITProfile.Protocol.HTTP;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MillClientTestIT  {

    public MillClientConfiguration.MillClientConfigurationBuilder clientConfig(TestITProfile profile) {
        val config = MillClientConfiguration.builder();

        config.host(profile.host())
              .port(profile.port());

        switch (profile.protocol()) {
            case GRPC -> config.protocol("grpc");
            case HTTP -> config.protocol(profile.tls() ? "https" : "http");
        }

        if (profile.protocol() == HTTP) {
            config.path("/services/jet");
        }

        return config;
    }

    public MillClient client(TestITProfile profile) {
        return MillClient.fromConfig(
                clientConfig(profile).build());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void handshake(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).handshake(
                HandshakeRequest.newBuilder().build());
        assertNotNull(resp);
        assertTrue(resp.getVersion() == ProtocolVersion.V1_0);
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void listSchemas(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).listSchemas(
                ListSchemasRequest.newBuilder().build());
        assertNotNull(resp);
        assertFalse(resp.getSchemasList().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void getSchema(TestITProfile profile) throws MillCodeException {
        val resp = client(profile).getSchema(
                GetSchemaRequest.newBuilder()
                        .setSchemaName("MONETA")
                        .build());
        assertNotNull(resp);
        assertFalse(resp.getSchema().getTablesList().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void execQuery(TestITProfile profile) throws MillCodeException {
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("SELECT `FIRST_NAME`, `CLIENT_ID` FROM `MONETA`.`CLIENTS` LIMIT 10")
                        .build())
                .build();

        val resp = client(profile).execQuery(request);
        assertNotNull(resp);
        assertTrue(resp.hasNext());
        val vector = resp.next().getVector();
        assertTrue(vector.hasSchema());
        assertEquals(2, vector.getSchema().getFieldsList().size());
    }

}