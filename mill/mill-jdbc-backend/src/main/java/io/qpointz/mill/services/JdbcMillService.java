package io.qpointz.mill.services;

import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.services.configuration.MillServiceConfiguration;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.configuration.JdbcServiceProvidersConfiguration;
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
