package io.qpointz.mill.service.calcite.configuration;

import io.qpointz.mill.service.calcite.CalciteContextFactory;
import io.qpointz.mill.service.calcite.ConnectionContextFactory;
import io.qpointz.mill.service.calcite.providers.CalcitePlanConverter;
import io.qpointz.mill.service.calcite.providers.PlanConverter;
import io.substrait.extension.ExtensionCollector;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="qp.mill.backend.calcite")
public class CalciteServiceProperties {

    @Getter
    @Setter
    private Map<String,Object> connection;


}
