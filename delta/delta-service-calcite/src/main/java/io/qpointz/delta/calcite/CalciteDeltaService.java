package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.configuration.CalciteConfiguration;
import io.qpointz.delta.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.delta.calcite.configuration.CalciteServiceCalciteContextConfiguration;
import io.qpointz.delta.service.*;
/* import io.qpointz.delta.service.ui.UIConfig; */
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
import lombok.val;


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