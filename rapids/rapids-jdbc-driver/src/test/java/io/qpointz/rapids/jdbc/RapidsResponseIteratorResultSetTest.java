package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.grpc.ExecQueryRequest;
import io.qpointz.rapids.grpc.ExecQueryStreamRequest;
import io.qpointz.rapids.grpc.RapidsDataServiceGrpc;
import io.qpointz.rapids.testing.H2Db;
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

@Slf4j
class RapidsResponseIteratorResultSetTest  {

    private static TestGrpcServer ctx;

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        ctx = TestGrpcServer.createServer();
    }

    @AfterAll
    public static void afterAll() {
        ctx.shutdown();
    }

    private java.util.Iterator<io.qpointz.rapids.grpc.ExecQueryResponse> iterable(String sql) {
        final var client = GrpcClient.client(TestGrpcServer.vertx);
        final SocketAddress socket = SocketAddress.inetSocketAddress(ctx.getServer().getPort(), "127.0.0.1");
        final var channel = new GrpcClientChannel(client, socket);
        final var met = RapidsDataServiceGrpc.newBlockingStub(channel);
        var sqlreq = ExecQueryRequest.newBuilder()
                .setSql(sql)
                .build();
        var req = ExecQueryStreamRequest.newBuilder()
                .setSqlRequest(sqlreq)
                .setBatchSize(53)
                .build();
        return  met.execQueryStream(req);
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