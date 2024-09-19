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
        assertEquals(Map.of(CLIENT_CHANNEL_PROP, CLIENT_CHANNEL_GRPC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void withScheme() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:grpc://host:1000");
        assertEquals(Map.of(CLIENT_CHANNEL_PROP, CLIENT_CHANNEL_GRPC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void inProcScheme() {
        val parser = MillUrlParser.parseUrl("jdbc:mill:in-proc://host:1000");
        assertEquals(Map.of(CLIENT_CHANNEL_PROP, CLIENT_CHANNEL_INPROC_VALUE, HOST_PROP, "host", PORT_PROP, "1000"), parser);
    }

    @Test
    void failsWithUnknownSchema() {
        assertThrows(IllegalArgumentException.class, () -> MillUrlParser.parseUrl("jdbc:mill:in-https://host:1000"));
    }


}