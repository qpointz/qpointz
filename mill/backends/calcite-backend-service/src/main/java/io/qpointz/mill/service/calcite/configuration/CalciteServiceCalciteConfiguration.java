package io.qpointz.mill.service.calcite.configuration;

import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.ConnectionContextFactory;
import io.substrait.extension.ExtensionCollector;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="qp.mill.backend.calcite")
public class CalciteServiceCalciteConfiguration {

    @Getter
    @Setter
    private Map<String,Object> connection;


    @Bean
    public CalciteContextFactory calciteConextFactory() {
        val props = new Properties();
        props.putAll(connection);
        return new ConnectionContextFactory(props);
    }

    @Bean
    public ExtensionCollector extensionCollector() {
        return new ExtensionCollector();
    }

}
