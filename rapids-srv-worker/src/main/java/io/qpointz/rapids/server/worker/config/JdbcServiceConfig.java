package io.qpointz.rapids.server.worker.config;

public interface JdbcServiceConfig {
    int port();

    boolean enabled();

    enum HandlerProtocol {
        JSON("json"),
        PROTOBUF("protobuf");

        public final String label;

        private HandlerProtocol(String label) {
            this.label = label;
        }
    }

    HandlerProtocol protocol();

}
