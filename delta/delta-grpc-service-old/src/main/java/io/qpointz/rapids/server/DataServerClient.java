package io.qpointz.rapids.server;

import io.grpc.Channel;
import io.qpointz.rapids.grpc.ExecQueryRequest;
import io.qpointz.rapids.grpc.ExecQueryStreamRequest;
import io.qpointz.rapids.grpc.RapidsDataServiceGrpc;
import io.qpointz.rapids.grpc.ResponseCode;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataServerClient {

    public static void main(String[] args) throws InterruptedException {
        final var vertx = Vertx.vertx();
        final var client = GrpcClient.client(vertx);
        final SocketAddress socket = SocketAddress.inetSocketAddress(8080, "localhost");

        final var channel = new GrpcClientChannel(client, socket);
        final var met = RapidsDataServiceGrpc.newBlockingStub(channel);

        var sql = "SELECT `city` AS `c1`, `id`,`state`,`city` FROM `airlines`.`cities`";
        var queryReq = ExecQueryRequest.newBuilder()
                .setSql(sql)
                .build();

        var req = ExecQueryStreamRequest.newBuilder()
                .setSqlRequest(queryReq)
                .setBatchSize(53)
                .build();

        final var iter = met.execQueryStream(req);

        if (!iter.hasNext()) {
            throw new RuntimeException("No response");
        }

        var fr = iter.next();
        var st = fr.getStatus();
        if (st.getCode()!= ResponseCode.OK) {
            log.error(st.getMessage());
            throw new RuntimeException(st.getMessage());
        }

        int cnt = 0;
        int records = 0;
        while (iter.hasNext()) {
            var b = iter.next();
            if (b.getStatus().getCode()!= ResponseCode.OK) {
                log.error("Failed on block {}, records got {}", ++cnt, records);
                throw new RuntimeException(b.getStatus().getMessage());
            }
            log.info("Block {}", ++cnt);
            records += b.getVector().getVectorSize();
            log.info("block records {}", b.getVector().getVectorSize());
            log.info("total Records {}", records);
            Thread.sleep(150);

        }



    }

}
