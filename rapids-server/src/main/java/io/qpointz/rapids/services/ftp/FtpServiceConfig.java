package io.qpointz.rapids.services.ftp;

import io.qpointz.rapids.services.AbstractBackgroundServiceConfig;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix=FtpServiceConfig.configurationPrefix)
public interface FtpServiceConfig extends AbstractBackgroundServiceConfig {
    static String configurationPrefix = "qpointz.rapids.services.ftp";
    int port();

    String host();

}
