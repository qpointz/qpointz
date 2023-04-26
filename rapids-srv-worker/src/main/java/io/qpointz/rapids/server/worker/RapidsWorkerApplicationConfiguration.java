package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.calcite.StandardCalciteHanlder;
import io.qpointz.rapids.server.worker.config.RapidsConfig;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Properties;

@Configuration
@Slf4j
public class RapidsWorkerApplicationConfiguration {

    @Bean
    public RapidsConfig config() {
        return new SmallRyeConfigBuilder()
                .withMapping(RapidsConfig.class)
                .addDefaultSources()
                .addDiscoveredConverters()
                .addDiscoveredSources()
                .build()
                .unwrap(SmallRyeConfig.class)
                .getConfigMapping(RapidsConfig.class);
    }

    @Bean
    public Vertx getVertx() {
        return Vertx.vertx();
    }

    @Bean
    CalciteHandler getCalciteHandler(RapidsConfig config) throws SQLException, ClassNotFoundException {
        log.warn("Runing calcite in standard mode");
        config.calcite();
        var props = new Properties();
        props.putAll(config.calcite().standard().properties());
        return new StandardCalciteHanlder(props);
    }

    @Bean
    public JdbcService jdbcService(RapidsConfig config, CalciteHandler calciteHandler) {
        return new JdbcService(config.services().jdbc(), calciteHandler);
    }



}
