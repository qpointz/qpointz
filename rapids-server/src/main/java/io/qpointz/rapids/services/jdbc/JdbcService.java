package io.qpointz.rapids.services.jdbc;


import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.services.AbstractBackgroundService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.*;

@ApplicationScoped
@Slf4j
public class JdbcService extends AbstractBackgroundService {

    private final JdbcServiceConfig jdbcServerConfig;
    private final CalciteHandler calciteHandler;


    public JdbcService(Vertx vertx, JdbcServiceConfig config, CalciteHandler calciteHandler) {
        super(vertx, config);
        //this.calcite = calciteService;
        this.calciteHandler = calciteHandler;
        this.jdbcServerConfig = config;
    }

    @Override
    protected String serviceName() {
        return "JDBC";
    }

    @Override
    protected String configurationPrefix() {
        return JdbcServiceConfig.configurationPrefix;
    }

    @Override
    protected void startService()  {
        final Meta meta = this.calciteHandler.getMeta();
        var localService = new LocalService(meta);

        AvaticaHandler handler = null;
        if (jdbcServerConfig.protocol()== JdbcServiceConfig.HandlerProtocol.JSON) {
            handler = new AvaticaJsonHandler(localService);
        } else if (jdbcServerConfig.protocol() == JdbcServiceConfig.HandlerProtocol.PROTOBUF) {
            handler = new AvaticaProtobufHandler(localService);
        } else {
            throw new RuntimeException(String.format("Unknwon protocol %s", jdbcServerConfig.protocol()));
        }

        final var srv = new HttpServer.Builder()
                .withHandler(handler)
                .withPort(jdbcServerConfig.port())
                .build();

        log.info("About to start JDBC HTTP Server");
        srv.start();
    }

}
