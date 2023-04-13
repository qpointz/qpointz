package io.qpointz.rapids.services.flight;

import io.qpointz.rapids.config.WorkerPoolConfig;
import io.qpointz.rapids.services.AbstractBackgroundServiceConfig;
import io.qpointz.rapids.services.ftp.FtpService;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix= FlightServiceConfig.configurationPrefix)
public interface FlightServiceConfig extends AbstractBackgroundServiceConfig {
    static String configurationPrefix = "qpointz.rapids.services.flight";
    int port();

    String host();

}
