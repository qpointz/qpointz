package io.qpointz.delta.calcite.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="calcite")
@AllArgsConstructor
@NoArgsConstructor
public class CalciteDataServiceConfiguration {

    @Getter
    @Setter
    private Map<String,Object> connection;

}
