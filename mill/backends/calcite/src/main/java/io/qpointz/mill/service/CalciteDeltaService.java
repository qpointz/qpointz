package io.qpointz.mill.service;

import io.qpointz.mill.service.calcite.configuration.CalciteConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceCalciteContextConfiguration;
import io.qpointz.mill.service.configuration.DeltaServiceConfiguration;
import io.qpointz.mill.service.configuration.security.OAuthConfiguration;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CalciteDeltaService {

    public static void main(String[] args) {
        val configuration = DeltaServiceConfiguration
                .newConfiguration()
                .withProviders(CalciteServiceProvidersContextConfiguration.class)
                .withAdditionalConfig(CalciteConfiguration.class)
                .withAdditionalConfig(CalciteServiceCalciteContextConfiguration.class)
                .withAdditionalConfig(OAuthConfiguration.class)
                .withDefaults();
        DeltaService.run(configuration ,args);
    }
}