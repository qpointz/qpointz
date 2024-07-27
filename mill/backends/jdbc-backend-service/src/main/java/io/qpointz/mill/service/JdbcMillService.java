package io.qpointz.mill.service;

import io.qpointz.mill.service.configuration.MillServiceConfiguration;
import io.qpointz.mill.service.jdbc.configuration.JdbcServiceProvidersConfiguration;
import lombok.val;

public class JdbcMillService {

    public static void main(String[] args) {
        val configuration = MillServiceConfiguration
                .newConfiguration()
                .withDefaults()
                .withProviders(JdbcServiceProvidersConfiguration.class)
                //.withAdditionalConfig(CalciteServiceCalciteConfiguration.class)
                .enableSecurity();
        MillService.run(configuration ,args);
    }
}
