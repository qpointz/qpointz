package io.qpointz.rapids.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;

public class ServerMain {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        final var grpcServer = GrpcServer.server(vertx);

        final var options = new HttpServerOptions();
        options.setPort(8080);
        options.setHost("localhost");


        final var server = vertx.createHttpServer(options);

        /*grpcServer.callHandler(GreeterGrpc.getSayHelloMethod(), request -> {
            request.handler(hello -> {
                var response = request.response();
                var reply = HelloReply.newBuilder().setReply("Hello " + hello.getName()).build();
                response.end(reply);
            });
        });*/

        server
                .requestHandler(grpcServer)
                .listen();

//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                System.out.println("Shutdown Hook is running !");
//            }
//        });
//        System.out.println("Application Terminating ...");
    }

}
