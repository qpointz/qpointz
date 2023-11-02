package io.qpointz.rapids.server;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CalciteDataServerConfig {

    @Getter
    private int port;

    @Getter
    private CalciteDataService service;

    public static class CalciteDataServerConfigBuilder {

        public CalciteDataServerConfigBuilder defaultConfig() {
            return this
                    .useDefaultPort();
        }

        public CalciteDataServerConfigBuilder useDefaultPort() {
            return this.port(8080);
        }

        public CalciteDataServerConfigBuilder useFreePort() {
            return this.port(0);
        }


    }

}
