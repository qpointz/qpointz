package io.qpointz.mill.service;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProvidersConfiguration;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceCalciteConfiguration;
import io.qpointz.mill.service.configuration.MillServiceConfiguration;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CalciteMillService {

    public static void main(String[] args) {
        val configuration = MillServiceConfiguration
                .newConfiguration()
                .withDefaults()
                .withProviders(CalciteServiceProvidersConfiguration.class)
                //.withAdditionalConfig(CalciteConfiguration.class)
                .withAdditionalConfig(CalciteServiceCalciteConfiguration.class)
                .enableSecurity();

        MillService.run(configuration ,args);
    }
}