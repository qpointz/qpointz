package io.qpointz.mill.service.calcite.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.Map;

@Configuration
//@EnableConfigurationProperties
//@ConfigurationProperties(prefix="qp.mill.backend.calcite")
@AllArgsConstructor
@NoArgsConstructor
public class CalciteConfiguration22 {

    @Getter
    @Setter
    private Map<String,Object> connection;

}
