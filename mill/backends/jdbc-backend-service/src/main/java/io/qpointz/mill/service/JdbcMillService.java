package io.qpointz.mill.service;

import io.qpointz.mill.service.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.service.configuration.MillServiceConfiguration;
import io.qpointz.mill.service.jdbc.configuration.JdbcCalciteConfiguration;
import io.qpointz.mill.service.jdbc.configuration.JdbcServiceProvidersConfiguration;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdbcMillService {

    public static void main(String[] args) {
        val configuration = MillServiceConfiguration
                .newConfiguration()
                .withDefaults()
                .withProviders(JdbcServiceProvidersConfiguration.class)
                .withAdditionalConfig(CalciteServiceProperties.class)
                .withAdditionalConfig(JdbcCalciteConfiguration.class)
                .enableSecurity();
        MillService.run(configuration ,args);
    }
}
