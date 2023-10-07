package io.qpointz.rapids.server.worker.config;

public interface ServicesConfig {
    JdbcServiceConfig jdbc();

    ODataServiceConfig odata();
}

