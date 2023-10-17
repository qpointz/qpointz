package io.qpointz.rapids.server;

import io.qpointz.rapids.formats.parquet.RapidsParquetSchema;
import io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.*;
import static io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory.RX_PATTERN_KEY;

@Slf4j
public class DataServer {

    private static Schema createSchema(SchemaPlus parentSchema, String name) {
        final var factory = new RapidsParquetSchemaFactory();
        final var operand = Map.<String,Object>of(
                FS_TYPE, "local",
                DIR_KEY, "./example/data/airlines_parquet",
                RX_DATASET_GROUP_KEY, "dataset",
                RX_PATTERN_KEY, ".*(\\/(?<dataset>[^\\/]+)\\.parquet$)"
        );
        final var schema = (RapidsParquetSchema)factory.create(parentSchema, name, operand);
        parentSchema.add(name, schema);
        return schema;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        final var vertx = Vertx.vertx();

        Class.forName("org.apache.calcite.jdbc.Driver");
        final var properties = new Properties();
        properties.put("quoting" , "BACK_TICK");
        properties.put("caseSensitive" , true);
        final var connection = DriverManager.getConnection("jdbc:calcite:");
        final var calcite = connection.unwrap(CalciteConnection.class);
        calcite.getRootSchema().add("airlines", createSchema(calcite.getRootSchema(),"airlines"));

        final var service = new CalciteDataService(calcite);

        final var server = VertxServerBuilder.forPort(vertx, 8080)
                .addService(service)
                .build();

        server.start();
        log.info("GRPC service started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.shutdown();
        }));

    }

}
