//package io.qpointz.rapids.jdbc;
//
//import io.qpointz.rapids.server.CalciteDataServer;
//import io.qpointz.rapids.server.CalciteDataServerConfig;
//import io.qpointz.rapids.server.CalciteDataServiceConfig;
//import io.qpointz.rapids.testing.H2Db;
//import io.vertx.core.Vertx;
//import io.vertx.grpc.VertxServer;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.SneakyThrows;
//
//import java.io.InputStreamReader;
//
//@Builder
//public class TestGrpcServer {
//
//    public static final Vertx vertx = Vertx.vertx();
//
//    @Getter
//    private final VertxServer server;
//
//    @Getter
//    private final H2Db db;
//
//    @SneakyThrows
//    public static TestGrpcServer createServer()  {
//        return TestGrpcServer.createServer("h2-test/sample.sql");
//    }
//
//    @SneakyThrows
//    public static TestGrpcServer createServer(String resourcePath)  {
//        final var stream = RapidsResponseIteratorResultSetTest
//                .class
//                .getClassLoader()
//                .getResourceAsStream(resourcePath);
//
//        final var reader = new InputStreamReader(stream);
//        final var db = H2Db.create("sample", reader);
//
//        final var service = CalciteDataServiceConfig.builder()
//                .defaultConfig()
//                .add(db.schemaFactory(), db.getSchemaName(), db.schemaOperand())
//                .buildService();
//
//        final var serverConfig = CalciteDataServerConfig.builder()
//                .defaultConfig()
//                .service(service)
//                .useFreePort()
//                .build();
//
//        final var server = CalciteDataServer.start(vertx, serverConfig);
//
//        return TestGrpcServer.builder()
//                .server(server)
//                .db(db)
//                .build();
//    }
//
//    @SneakyThrows
//    public void shutdown() {
//        this.getServer().shutdown();
//        this.getDb().close();
//    }
//}
