package io.qpointz.mill.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.proto.HandshakeResponse;
import io.qpointz.mill.proto.ProtocolVersion;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.internal.duplex.DuplexResponseBody;
import okio.Buffer;
import okio.ByteString;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;


import static org.junit.jupiter.api.Assertions.*;

class HttpMillClientTest {

    private Buffer fromMessage(Message message) throws IOException {
        val strm = new ByteArrayInputStream(message.toByteArray());
        val b = new Buffer();
        b.readFrom(strm);
        return b;
    }

    @SneakyThrows
    @Test
    void mockServerSimple() throws IOException {
        try (val srv = new MockWebServer()) {
            val msg = HandshakeResponse.newBuilder()
                    .setVersion(ProtocolVersion.V1_0)
                    .setAuthentication(HandshakeResponse.AuthenticationContext.newBuilder()
                            .setName("ANON").build()).build();

            val eresp = new MockResponse()
                    .setResponseCode(200)
                    .setBody(fromMessage(msg));

            srv.enqueue(eresp);
            srv.start();

            val client = HttpMillClient.builder()
                    .url(srv.url("/api").toString())
                    .build();

            val resp = client.handshake(HandshakeRequest.getDefaultInstance());
            assertNotNull(resp);
            assertEquals(ProtocolVersion.V1_0, resp.getVersion());

            val req = srv.takeRequest();
            assertEquals(srv.url("/api/Handshake"), req.getRequestUrl());
        }
    }



}