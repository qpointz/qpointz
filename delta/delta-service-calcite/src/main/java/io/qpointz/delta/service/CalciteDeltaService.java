package io.qpointz.delta.service;

import io.qpointz.delta.service.calcite.configuration.CalciteConfiguration;
import io.qpointz.delta.service.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.delta.service.calcite.configuration.CalciteServiceCalciteContextConfiguration;
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
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
                .withDefaults();
        DeltaService.run(configuration ,args);
    }
}