package io.qpointz.rapids.services.jdbc;


import io.qpointz.rapids.config.WorkerPoolConfig;
import io.qpointz.rapids.services.AbstractBackgroundServiceConfig;
import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix=JdbcServiceConfig.configurationPrefix)
public interface JdbcServiceConfig extends AbstractBackgroundServiceConfig {

    static String configurationPrefix =  "qpointz.rapids.services.jdbc";
    enum HandlerProtocol {
        JSON("json"),
        PROTOBUF("protobuf");

        public final String label;
        private HandlerProtocol(String label) {
            this.label = label;
        }
    }
    int port();

    HandlerProtocol protocol();

}
