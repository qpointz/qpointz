package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.server.worker.config.ODataServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.openid.OpenIdAuthenticator;
import org.eclipse.jetty.security.openid.OpenIdConfiguration;
import org.eclipse.jetty.security.openid.OpenIdLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;

@Slf4j
public class ODataService extends AbstractService {

    private final CalciteHandler calciteHandler;
    private final ODataServiceConfig config;
    private Server httpServer;

    public ODataService(ODataServiceConfig config, CalciteHandler calciteHandler) {
        super("ODATA", config.enabled());
        this.calciteHandler = calciteHandler;
        this.config = config;
    }

    @Override
    protected void startService() throws Exception {
        log.info("Configuring ODATA http server");
        this.httpServer = new Server(this.config.port());
        log.debug("Creating http handler");
        final var rootSchema = this.calciteHandler.getRootSchema();
        final var sh = new ServletContextHandler(ServletContextHandler.SESSIONS);
        for (final var schemaName : rootSchema.getSubSchemaNames()) {
            final var serviceName = "/%s.svc".formatted(schemaName).toLowerCase();
            log.info("Mapping {} service to {} schema", serviceName, schemaName);
            final var schema = rootSchema.getSubSchema(schemaName);
            final var holder = ODataServlet.create(schema, this.config.namespace(), this.calciteHandler);
            sh.addServlet(new ServletHolder(holder), serviceName+"/*");
        }
        //sh.setSecurityHandler(securityHandler());
        this.httpServer.setHandler(sh);
        log.info("Starting http server");
        this.httpServer.start();
    }


    private SecurityHandler securityHandler() {
        final var con = new Constraint();
        con.setName(Constraint.__BASIC_AUTH);
        con.setAuthenticate(true);
        con.setRoles(new String[] {"reader"});

        final var conMap = new ConstraintMapping();
        conMap.setConstraint(con);
        conMap.setPathSpec("/airlines.svc/*");

        return basicAuth(conMap);
    }

    private SecurityHandler openIdAuth(ConstraintMapping conMap) {
        final var config = new OpenIdConfiguration("",
                "",
                "");
        //ee1edf86-1c09-4dea-9c73-cc228f379a2d
        final var csh = new ConstraintSecurityHandler();
        csh.addConstraintMapping(conMap);
        csh.setAuthenticator(new OpenIdAuthenticator(config, ""));
        //csh.setRealmName(realm);
        csh.setLoginService(new OpenIdLoginService(config));
        return csh;
    }

    private SecurityHandler basicAuth(ConstraintMapping conMap) {
        final var realm = "myrealm";

//        final var userStore = new UserStore();
//        userStore.addUser("test", new Password("test"), new String[]{"user"});

        final var hls = new HashLoginService(realm, "./config/passwd");
        hls.setName(realm);
        hls.setHotReload(true);
//        hls.setUserStore(userStore);

        final var csh = new ConstraintSecurityHandler();
        csh.addConstraintMapping(conMap);
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName(realm);
        csh.setLoginService(hls);

        return csh;
    }


    @Override
    protected void stopService() throws Exception {
        this.httpServer.stop();
    }
}
