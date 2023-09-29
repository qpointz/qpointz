package io.qpointz.rapids.grpc.server;

import io.qpointz.rapids.grpc.GetNameRequest;
import io.qpointz.rapids.grpc.NamerGrpc;
import io.qpointz.rapids.grpc.VertxNamerGrpcClient;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        GrpcClient cl = GrpcClient.client(vertx);
        final var so = SocketAddress.inetSocketAddress(18030, "localhost");
        final var c = new VertxNamerGrpcClient(cl, so);
        var nm = NamerGrpc.getGetNameMethod();
        c.getName(GetNameRequest.newBuilder().setName("foo").build()).onSuccess(resp -> {
            log.info(resp.getName());
        });
    }

}
