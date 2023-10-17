package io.qpointz.rapids.server;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientChannel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServerClient {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        final var client = GrpcClient.client(vertx);

        final var socket = SocketAddress.inetSocketAddress(8080, "localhost");
        final var channel = new GrpcClientChannel(client, socket);
        final var stub = GreeterGrpc.newStub(channel);

        final var request = HelloRequest.newBuilder()
                .setName("Bob 22")
                .build();

        stub.sayHello(request, new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
                log.info("Next value:{}", value.getReply());
            }

            @Override
            public void onError(Throwable t) {
                log.error("Request failed", t);
            }

            @Override
            public void onCompleted() {
                log.info("Request completed");
            }
        });

    }

}
