package io.qpointz.mill.client;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.qpointz.mill.client.MillClientConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;

class MillUrlParserTest {

    @Test
    void parseDefault() {
        val parser = MillUrlParser.parseUrl("jdbc:mill://host:1000");
        assertEquals(Map.of(CLIENT_PROTOCOL_PROP, CLIENT_PROTOCOL_GRPC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void withScheme() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:grpc://host:1000");
        assertEquals(Map.of(CLIENT_PROTOCOL_PROP, CLIENT_PROTOCOL_GRPC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void inProcScheme() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:mem://host:1000");
        assertEquals(Map.of(CLIENT_PROTOCOL_PROP, CLIENT_PROTOCOL_IN_PROC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void failsWithUnknownSchema() {
        assertThrows(IllegalArgumentException.class, () -> MillUrlParser.parseUrl("jdbc:mill:in-https://host:1000"));
    }

    @Test
    void parsesUserInfoCredentials() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:grpc://admin:secret@localhost:9090");
        assertEquals("admin", parser.getProperty(USERNAME_PROP));
        assertEquals("secret", parser.getProperty(PASSWORD_PROP));
        assertEquals("9090", parser.getProperty(PORT_PROP));
    }

    @Test
    void omitsPortWhenNotInUrl() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:grpc://localhost");
        assertNull(parser.getProperty(PORT_PROP));
    }

    @Test
    void mergesConnectionPropertiesWithUsernameAlias() {
        val info = new java.util.Properties();
        info.setProperty("username", "admin");
        info.setProperty("password", "secret");
        val merged = MillUrlParser.apply("jdbc:mill:grpc://localhost:9090", info);
        val config = MillClientConfiguration.builder().fromProperties(merged).build();
        assertEquals("admin", config.getUsername());
        assertEquals("secret", config.getPassword());
    }

    @Test
    void httpsCloudRunUrlWithoutPortDoesNotDefaultTo8080() {
        val merged = MillUrlParser.apply("jdbc:mill:https://mld6-run-service.example.run.app");
        val config = MillClientConfiguration.builder().fromProperties(merged).build();
        assertEquals(CLIENT_PROTOCOL_HTTPS_VALUE, config.getProtocol());
        assertTrue(config.getPort() <= 0, "HTTPS without port must not default to 8080");
    }

    @Test
    void urlEmbeddedCredentialsSurviveEmptyDbeaverProperties() {
        val info = new java.util.Properties();
        info.setProperty("user", "");
        info.setProperty("password", "");
        val merged = MillUrlParser.apply("jdbc:mill:https://admin:secret@host/services/jet", info);
        val config = MillClientConfiguration.builder().fromProperties(merged).build();
        assertEquals("admin", config.getUsername());
        assertEquals("secret", config.getPassword());
    }

    @Test
    void explicitConnectionPropertiesOverrideUrlCredentials() {
        val info = new java.util.Properties();
        info.setProperty("user", "other");
        info.setProperty("password", "override");
        val merged = MillUrlParser.apply("jdbc:mill:https://admin:secret@host/services/jet", info);
        val config = MillClientConfiguration.builder().fromProperties(merged).build();
        assertEquals("other", config.getUsername());
        assertEquals("override", config.getPassword());
    }

    @Test
    void httpsClientUrlOmitsPortForCloudRunHost() {
        val client = HttpMillClient.builder()
                .protocol("https")
                .host("mld6-run-service.example.run.app")
                .port(-1)
                .path("/services/jet/")
                .useBasicAuthentication("admin", "secret")
                .build();
        assertEquals("https://mld6-run-service.example.run.app/services/jet/", client.getClientUrl());
    }

}