package io.qpointz.rapids.server.worker.config;

public interface ODataServiceConfig {

    int port();
    boolean enabled();

    String namespace();

}
