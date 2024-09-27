package io.qpointz.mill.services;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProvidersConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.services.configuration.MillServiceConfiguration;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CalciteMillService {

    public static void main(String[] args) {
        val configuration = MillServiceConfiguration
                .newConfiguration()
                .withDefaults()
                .withProviders(CalciteServiceProvidersConfiguration.class)
                .withAdditionalConfig(CalciteServiceProperties.class)
                .withAdditionalConfig(CalciteServiceConfiguration.class)
                .enableSecurity();

        MillService.run(configuration ,args);
    }
}