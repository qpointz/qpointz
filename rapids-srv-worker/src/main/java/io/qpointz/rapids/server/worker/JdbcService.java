package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.server.worker.config.JdbcServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.AvaticaHandler;
import org.apache.calcite.avatica.server.AvaticaJsonHandler;
import org.apache.calcite.avatica.server.AvaticaProtobufHandler;
import org.apache.calcite.avatica.server.HttpServer;

import java.nio.file.spi.FileSystemProvider;
import java.sql.SQLException;


@Slf4j
public class JdbcService extends AbstractService {

    private final JdbcServiceConfig config;
    private final CalciteHandler calciteHandler;

    public JdbcService(JdbcServiceConfig config, CalciteHandler calciteHandler) {
        super("JDBC", config.enabled());
        this.config = config;
        this.calciteHandler = calciteHandler;
    }

    @Override
    protected void startService()  throws SQLException, ClassNotFoundException {
        final Meta meta = this.calciteHandler.getMeta();
        var localService = new LocalService(meta);

        log.info("Installed filesystem providers:Service start");
        FileSystemProvider.installedProviders().forEach(p -> {
            log.info("Provider '%s'".formatted(p.getScheme()));
        });

        AvaticaHandler handler = null;
        if (this.config.protocol()== JdbcServiceConfig.HandlerProtocol.JSON) {
            handler = new AvaticaJsonHandler(localService);
        } else if (this.config.protocol() == JdbcServiceConfig.HandlerProtocol.PROTOBUF) {
          handler = new AvaticaProtobufHandler(localService);
        } else {
            throw new RuntimeException(String.format("Unknwon protocol %s", this.config.protocol()));
        }

        final var srv = new HttpServer.Builder()
                .withHandler(handler)
                .withPort(this.config.port())
                .build();

        log.info("About to start JDBC HTTP Server");
        srv.start();
    }



}
