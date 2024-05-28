package io.qpointz.rapids.server;

import io.vertx.grpc.VertxServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

@Slf4j
public class GrpcService {

    public static void main(String[] args) throws IOException {
        log.info("Starting service");
        var ctx = new AnnotationConfigApplicationContext(GrpcServiceApplicationConfiguration.class);
        log.info("Reading configuration");

        var server = ctx.getBean(VertxServer.class);

        server.start();
        //log.info("Server started on port {}", server.getPort());
        log.info("Started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.shutdown();
        }));
    }

}
