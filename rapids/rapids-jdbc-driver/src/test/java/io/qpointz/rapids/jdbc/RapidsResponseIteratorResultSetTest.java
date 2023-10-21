package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.grpc.ExecSqlRequest;
import io.qpointz.rapids.grpc.RapidsDataServiceGrpc;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RapidsResponseIteratorResultSetTest  {

    private static Vertx vertx = Vertx.vertx();
    private static VertxServer server;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        final var stream = RapidsResponseIteratorResultSetTest.class.getClassLoader().getResourceAsStream("h2-samples/sample.sql");
        final var reader = new InputStreamReader(stream);
        server = H2ServerTestUtils.startSample(vertx, "sample", reader);
    }

    @AfterAll
    public static void afterAll() {
        H2ServerTestUtils.stopServer(server);
    }

    private java.util.Iterator<io.qpointz.rapids.grpc.ExecSqlResponse> iterable(String sql) {
        final var vertx = Vertx.vertx();
        final var client = GrpcClient.client(vertx);
        final SocketAddress socket = SocketAddress.inetSocketAddress(server.getPort(), "127.0.0.1");
        final var channel = new GrpcClientChannel(client, socket);
        final var met = RapidsDataServiceGrpc.newBlockingStub(channel);
        var req = ExecSqlRequest.newBuilder()
                .setBatchSize(53)
                .setSql(sql)
                .build();
        return  met.execSqlBatched(req);
    }


    @Test
    void trivia() throws SQLException {
        final var iter = iterable("SELECT * FROM `sample`.`T1`");
        final var rs = ResultSets.from(iter);
        final var v1 = new ArrayList<String>();
        final var v2 = new ArrayList<Integer>();
        int i =0;
        while (rs.next()) {
            v1.add(rs.getString("V1"));
            v2.add(rs.getInt("V2"));
            log.debug("{v1={},v2={}}", v1.get(i), v2.get(i) );
            i++;
        }
    }

}