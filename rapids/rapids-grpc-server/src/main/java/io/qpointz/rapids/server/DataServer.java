package io.qpointz.rapids.server;

import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlConformance;
import org.apache.calcite.sql.validate.SqlConformanceEnum;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.*;
import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.RX_PATTERN_KEY;

@Slf4j
public class DataServer {

    private static void createSchema(CalciteDataServiceConfig.CalciteDataServiceConfigBuilder config , String name) {
        final var factory = new RapidsParquetSchemaFactory();
        final var operand = Map.<String,Object>of(
                FS_TYPE, "local",
                DIR_KEY, "./../example/data/airlines_parquet",
                RX_DATASET_GROUP_KEY, "dataset",
                RX_PATTERN_KEY, ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)"
        );
        config.add(factory, name, operand);
    }

    public static void main(String[] args) throws IOException {
        final var serviceConfig = CalciteDataServiceConfig.builder()
                .defaultConfig();

        createSchema(serviceConfig, "airlines");

        final var service = serviceConfig.buildService();

        final var serverConfig = CalciteDataServerConfig.builder()
                .defaultConfig()
                .service(service)
                .build();

        final var vertx = Vertx.vertx();

        log.info("About to start service");
        final var server = CalciteDataServer.start(vertx, serverConfig);
        log.info("Service started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.shutdown();
        }));

    }

}
