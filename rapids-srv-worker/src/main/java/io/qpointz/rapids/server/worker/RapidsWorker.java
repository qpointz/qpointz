package io.qpointz.rapids.server.worker;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

@Slf4j
public class RapidsWorker {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        log.info("Worker is starting");
        var i = 1;
        log.info("Arguments");
        for (var a : args) {
            log.info("Arg {}:{}", i++, a);
        }
        var ctx = new AnnotationConfigApplicationContext(RapidsWorkerApplicationConfiguration.class);


        var vertx = ctx.getBean(Vertx.class);
        var services = ctx.getBean(JdbcService.class);

        vertx.deployVerticle(services);

        Thread haltedHook = new Thread(() -> log.info("Exiting"));
        Runtime.getRuntime().addShutdownHook(haltedHook);
    }

}
