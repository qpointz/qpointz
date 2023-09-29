package io.qpointz.rapids.grpc.server;

import io.qpointz.rapids.grpc.GetNameRequest;
import io.qpointz.rapids.grpc.GetNameResponse;
import io.qpointz.rapids.grpc.VertxNamerGrpcServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.server.GrpcServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(18030);

        HttpServer server = vertx.createHttpServer(options);
        GrpcServer grpcServer = GrpcServer.server(vertx);
        VertxNamerGrpcServer.NamerApi stub = new VertxNamerGrpcServer.NamerApi() {
            @Override
            public Future<GetNameResponse> getName(GetNameRequest request) {
                return Future.succeededFuture(GetNameResponse.newBuilder()
                        .setName(request.getName())
                        .setKnown("OK")
                        .setStatus(1)
                        .build()
                );
            }
        };

        stub.bindAll(grpcServer);

        log.info("Starting");
        server.requestHandler(grpcServer).listen();

        Thread printingHook = new Thread(() -> log.info("Exiting"));
        Runtime.getRuntime().addShutdownHook(printingHook);


    }

}
