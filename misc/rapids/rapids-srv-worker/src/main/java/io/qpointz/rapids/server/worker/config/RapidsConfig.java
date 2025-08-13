package io.qpointz.rapids.server.worker.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "rapids")
public interface RapidsConfig {

    ServicesConfig services();

    CalciteConfig calcite();
}
