package io.qpointz.mill.client;

import io.qpointz.mill.BaseTest;
import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.proto.HandshakeRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MillClientTest extends BaseTest {

    @Test
    void blockingStubConnect() throws MillCodeException {
        val clientCfg = MillClientConfiguration.builder()
                .host(getMillHost())
                .port(getMillPort())
                .build();
        val client = MillClient.fromConfig(clientCfg);
        val resp  = client.handshake(HandshakeRequest.newBuilder().build());
        assertNotNull(resp);
    }

    void connectAndTest(String tag, MillClient client, Function<String, Boolean> principalEval) throws MillCodeException {
        try {
            val resp = client.handshake(HandshakeRequest.newBuilder().build());
            assertNotNull(resp);
            val name = resp.getAuthentication().getName();
            log.info(String.format("User name %s", name));
            assertTrue(principalEval.apply(name));
        } catch (Exception ex) {
            log.error("Test failed", ex);
            throw ex;
        }
    }

    @Test
    void connectNoAuthNoTls() throws MillCodeException {
        val client = MillClientConfiguration.builder()
                .host(getMillHost())
                .port(getMillPort())
                .buildClient();
        connectAndTest("Anonymous no auth no tls", client, k-> k.equals("ANONYMOUS"));
    }


    @Test
    void connectAuthTlsBasicAuth() throws MillCodeException {
        val client = MillClientConfiguration.builder()
                .host(getMillAuthTlsHost())
                .port(getMillPort())
                .username(getMillUser())
                .password(getMillPassword())
                .tlsKeyCertChain(getTlsCertChain())
                .tlsKeyPrivateKey(getTlsCertPk())
                .tlsTrustRootCert(getTlsRootCa())
                .buildClient();
        connectAndTest("Basic authentication no tls", client, k-> k.equals("reader"));
    }

    @Test
    void connectAuthTlsBearerTokenAuth() throws MillCodeException {
        val client = MillClientConfiguration.builder()
                .host(getMillAuthTlsHost())
                .port(getMillPort())
                .bearerToken(getMillJwtToken())
                .tlsKeyCertChain(getTlsCertChain())
                .tlsKeyPrivateKey(getTlsCertPk())
                .tlsTrustRootCert(getTlsRootCa())
                .buildClient();
        connectAndTest("Bearer token authentication no tls", client, k-> !k.equals("ANONYMOUS"));
    }

    @Test
    void connectAuthTlsMultiCredentialsTokenAuth() throws MillCodeException {
        val client = MillClientConfiguration.builder()
                .host(getMillAuthTlsHost())
                .port(getMillPort())
                .bearerToken(getMillJwtToken())
                .username(getMillUser())
                .password(getMillPassword())
                .tlsKeyCertChain(getTlsCertChain())
                .tlsKeyPrivateKey(getTlsCertPk())
                .tlsTrustRootCert(getTlsRootCa())
                .buildClient();
        connectAndTest("Bearer token authentication no tls", client, k-> !k.equals("ANONYMOUS") && !k.equals("reader"));
    }




}