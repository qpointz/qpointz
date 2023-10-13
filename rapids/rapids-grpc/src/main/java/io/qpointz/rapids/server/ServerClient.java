package io.qpointz.rapids.server;

import io.grpc.Grpc;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;

public class ServerClient {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        final var client = GrpcClient.client(vertx);
/*
        var server = SocketAddress.inetSocketAddress(8080, "localhost");
        final var method = GreeterGrpc.getSayHelloMethod();
        final var fur = client.request(server, method);
        fur.onSuccess(request -> {
            request.end(HelloRequest.newBuilder().setName("Bob 22").build());
            request.response().onSuccess(response -> {
               var reply = response.last();
               reply.onSuccess(rep -> {
                  System.out.println("Recs:" + rep.getReply());
               });
            });
        }); */
    }

}
