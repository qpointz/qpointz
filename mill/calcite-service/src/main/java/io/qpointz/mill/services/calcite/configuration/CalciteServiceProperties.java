package io.qpointz.mill.services.calcite.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="mill.backend.calcite")
public class CalciteServiceProperties {

    @Getter
    @Setter
    private Map<String,Object> connection;


}
