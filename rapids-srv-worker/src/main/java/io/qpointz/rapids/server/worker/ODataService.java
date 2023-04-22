package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.server.worker.config.ODataServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.SchemaPlus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@Slf4j
public class ODataService extends AbstractService {

    private final CalciteHandler calciteHandler;
    private final ODataServiceConfig config;
    private Server httpServer;
    private ServerConnector connector;

    public ODataService(ODataServiceConfig config, CalciteHandler calciteHandler) {
        super("ODATA", config.enabled());
        this.calciteHandler = calciteHandler;
        this.config = config;
    }

    @Override
    protected void startService() throws Exception {
        log.info("Configuring ODATA http server");
        this.httpServer = new Server();
        this.connector = new ServerConnector(this.httpServer);
        this.connector.setPort(this.config.port());

        log.debug("Creating http handler");
        var sh = new ServletHandler();
        final var rootSchema = this.calciteHandler.getRootSchema();
        for (final var schemaName : rootSchema.getSubSchemaNames()) {
            final var serviceName = "/%s.svc/*".formatted(schemaName);
            log.info("Mapping {} service to {} schema", serviceName, schemaName);
            final var schema = rootSchema.getSubSchema(schemaName);
            final var holder = ODataServlet.create(schema, this.config.namespace());
            sh.addServletWithMapping(holder, serviceName);
        }
        this.httpServer.setHandler(sh);
        log.info("Starting http server");
        this.httpServer.start();
    }

    @Override
    protected void stopService() throws Exception {
        this.httpServer.stop();
    }
}
