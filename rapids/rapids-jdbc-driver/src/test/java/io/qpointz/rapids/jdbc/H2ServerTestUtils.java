package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.server.CalciteDataServer;
import io.qpointz.rapids.server.CalciteDataServerConfig;
import io.qpointz.rapids.server.CalciteDataServiceConfig;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.Driver;
import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class H2ServerTestUtils {

    @Builder
    public static class H2Info {

        @Getter
        private String userName;

        @Getter
        private String password;

        @Getter
        private String url;

        @Getter
        private String driver;

        @Getter
        private Path tempDir;

    }

    public static H2Info createH2Server(String schemaName, Reader scriptReader) throws ClassNotFoundException, SQLException, IOException {

//        final var temp = Files
//                .createTempDirectory("rapids-"+schemaName)
//                .toFile()
//                .getCanonicalFile()
//                .toPath();

        final var driverName = "org.h2.Driver";
        Class.forName(driverName);
        Class.forName("org.apache.calcite.jdbc.Driver");

        //final var url = String.format("jdbc:h2:file:%s;DB_CLOSE_ON_EXIT=FALSE", temp);
        final var url = String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", schemaName);

        final var username = "admin";
        final var password = "password";
        final var conn = DriverManager.getConnection(
                url , username, password);
        final var infoBuilder = H2Info.builder()
                .userName(username)
                .password(password)
                .driver(driverName)
                .url(url);
                //.tempDir(temp.toFile().getCanonicalFile().toPath());
        if (scriptReader!=null) {
            RunScript.execute(conn, scriptReader);
        }
        return infoBuilder.build();
    }


    public static VertxServer startServer(Vertx vertx, String schemaName, Reader scriptReader) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        final var info  = createH2Server(schemaName, scriptReader);
        Map<String,Object> operand = Map.of(
                "jdbcUrl", info.url,
                "jdbcUser", info.userName,
                "jdbcPassword", info.password,
                "jdbcDriver", info.driver

        );

        final var conn = DriverManager
                .getConnection("jdbc:calcite:")
                .unwrap(CalciteConnection.class);

        final var service = CalciteDataServiceConfig.builder()
                .defaultConfig()
                .add(JdbcSchema.Factory.INSTANCE, schemaName, operand)
                .buildService();

        final var serverConfig = CalciteDataServerConfig.builder()
                .defaultConfig()
                .service(service)
                .build();

        final var server = CalciteDataServer.start(vertx, serverConfig);
        Thread.sleep(100);
        return server;
    }


    public static VertxServer startSample(Vertx vertx, String schemaName, Reader reader) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        //final var resourcePath = String.format("h2-samples/%s.sql", sampleName);
        //final var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        //Reader reader = new InputStreamReader(inputStream);
        return startServer(vertx, schemaName, reader);
    }

    public static void stopServer(VertxServer server) {
        if (server!=null) {
            server.shutdown();
        }
    }


}
