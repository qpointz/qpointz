package io.qpointz.rapids.server;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServiceApplicationConfiguration {

    @Bean
    public GrpcServiceConfig config() {
        return new SmallRyeConfigBuilder()
                .withMapping(GrpcServiceConfig.class)
                .addDefaultSources()
                .addDiscoveredConverters()
                .addDiscoveredSources()
                .build()
                .unwrap(SmallRyeConfig.class)
                .getConfigMapping(GrpcServiceConfig.class);
    }

    @Bean
    public Vertx getVertx() {
        return Vertx.vertx();
    }


    @Bean
    public CalciteDataServerConfig dataServerConfig(CalciteDataService service, GrpcServiceConfig serviceConfig) {
        return CalciteDataServerConfig
                .builder()
                .defaultConfig()
                .service(service)
                .port(serviceConfig.port())
                .build();
    }

    @Bean
    public VertxServer dataServer(Vertx vertx, CalciteDataServerConfig calciteDataServerConfig) throws IOException {
        return CalciteDataServer.start(vertx, calciteDataServerConfig);
    }


    @Bean
    public CalciteDataServiceConfig dataServiceConfig() {
        return CalciteDataServiceConfig.builder()
                .defaultConfig()
                .build();
    }

    @Bean
    public CalciteDataService dataService(CalciteDataServiceConfig config) {
        return CalciteDataService
                .create(config);
    }


}
