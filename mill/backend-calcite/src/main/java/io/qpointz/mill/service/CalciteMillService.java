package io.qpointz.mill.service;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersContextConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceCalciteContextConfiguration;
import io.qpointz.mill.service.configuration.MillServiceConfiguration;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CalciteMillService {

    public static void main(String[] args) {
        val configuration = MillServiceConfiguration
                .newConfiguration()
                .withDefaults()
                .withProviders(CalciteServiceProvidersContextConfiguration.class)
                //.withAdditionalConfig(CalciteConfiguration.class)
                .withAdditionalConfig(CalciteServiceCalciteContextConfiguration.class)
                .enableSecurity();

        MillService.run(configuration ,args);
    }
}