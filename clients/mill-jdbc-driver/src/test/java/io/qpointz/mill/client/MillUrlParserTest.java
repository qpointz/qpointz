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

}