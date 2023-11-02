package io.qpointz.rapids.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CalciteDataServer {

    public static VertxServer start(Vertx vertx, CalciteDataServerConfig config) throws IOException {
        final var server = VertxServerBuilder.forPort(vertx, 0)
                .addService(config.getService())
                .build();

        final var latch = new CountDownLatch(1);
        server.start(ar -> {
            log.info("Start callback");
            latch.countDown();
        });

        try {
            log.info("Waiting server to start");
            latch.await(3000, TimeUnit.MILLISECONDS);
            log.info("test grpc Server started");
            return server;
        }
        catch (InterruptedException ex) {
            log.error("Failed to start test grpc server", ex);
            throw new RuntimeException("Failed to start");
        }
    }

}
