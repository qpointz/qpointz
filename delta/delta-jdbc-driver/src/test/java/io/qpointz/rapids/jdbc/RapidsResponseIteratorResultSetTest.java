package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.grpc.ExecQueryRequest;
import io.qpointz.rapids.grpc.ExecQueryStreamRequest;
import io.qpointz.rapids.grpc.RapidsDataServiceGrpc;
import io.qpointz.rapids.testing.H2Db;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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

    private ResultSet resultSet(String sql) {
        final var iter = iterable(sql);
        return ResultSets.from(iter);
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

    @Test
    void isBeforeFirstReturns() throws SQLException {
        final var rs = resultSet("SELECT * FROM `sample`.`T1`");
        assertTrue(rs.isBeforeFirst());
        while (rs.next()) {
            assertFalse(rs.isBeforeFirst());
        }
    }

    @Test
    void isFirstReturns() throws SQLException {
        final var rs = resultSet("SELECT * FROM `sample`.`T1`");
        rs.next();
        assertTrue(rs.isFirst());
        while (rs.next()) {
            assertFalse(rs.isFirst());
        }
    }

    @Test
    void isLastReturns() throws SQLException {
        final var cntRs = resultSet("SELECT COUNT(*) AS CNT FROM `sample`.`T1`");
        cntRs.next();
        final var cnt = cntRs.getLong(0);
        assertTrue(cnt>1);

        final var rs = resultSet("SELECT * FROM `sample`.`T1`");

        for (var i=0;i<cnt;i++) {
            rs.next();
            if (i<cnt-1) {
                assertFalse(rs.isLast());
            } else {
                assertTrue(rs.isLast());
            }
        }
    }

    @Test
    void isAfterLastReturns() throws SQLException {
        final var rs = resultSet("SELECT * FROM `sample`.`T1`");
        while(rs.next()) {
            assertFalse(rs.isAfterLast());
        }
        assertTrue(rs.isAfterLast());
    }



}