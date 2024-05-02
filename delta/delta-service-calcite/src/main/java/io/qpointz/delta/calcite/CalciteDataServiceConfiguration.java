package io.qpointz.delta.calcite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="calcite")
@AllArgsConstructor
@NoArgsConstructor
public class CalciteDataServiceConfiguration {

    @Getter
    @Setter
    private Map<String,Object> connection;

}
