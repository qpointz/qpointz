package io.qpointz.rapids.server;

import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

import java.io.IOException;
import java.net.SocketAddress;

public class CalciteDataServer {

    public static VertxServer start(Vertx vertx, CalciteDataServerConfig config) throws IOException {
        final var server = VertxServerBuilder.forPort(vertx, config.getPort())
                .addService(config.getService())
                .build();
        server.start();
        return server;
    }

}
