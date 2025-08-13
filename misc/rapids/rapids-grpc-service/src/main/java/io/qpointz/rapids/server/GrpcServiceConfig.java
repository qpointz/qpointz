package io.qpointz.rapids.server;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "rapids.grpc")
public interface GrpcServiceConfig {
    int port();

}
