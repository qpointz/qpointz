package io.qpointz.rapids.services.ftp;

import io.qpointz.rapids.services.AbstractBackgroundService;
import io.qpointz.rapids.services.AbstractBackgroundServiceConfig;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;

@Slf4j
public class FtpService extends AbstractBackgroundService {

    private final FtpServiceConfig serviceConfig;

    public FtpService(Vertx vertx, FtpServiceConfig serviceConfig) {
        super(vertx, serviceConfig);
        this.serviceConfig = serviceConfig;
    }

    @Override
    protected String serviceName() {
        return "FTP";
    }

    @Override
    protected String configurationPrefix() {
        return FtpServiceConfig.configurationPrefix;
    }

    @Override
    protected void startService() {
        var serverFactory = new FtpServerFactory();

        var ftpListener = new ListenerFactory();
        ftpListener.setPort(this.serviceConfig.port());
        serverFactory.addListener("default", ftpListener.createListener());

        var server = serverFactory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            throw new RuntimeException(e);
        }
    }
}
