package io.qpointz.mill.azure.functions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = "io.qpointz.mill")
@Import({BackendFunctions.class})
@Slf4j
public class FunctionApplication {

    public static void main(String[] args) {
//        val configuration = MillServiceConfiguration
//                .newConfiguration()
//                .withDefaults()
//                .withProviders(JdbcServiceProvidersConfiguration.class)
//                .withAdditionalConfig(CalciteServiceProperties.class)
//                .withAdditionalConfig(JdbcCalciteConfiguration.class)
//                .withAdditionalConfig(BackendFunctions.class)
//                .withAdditionalConfig(AzureBackendFunctions.class)
//                .configsToArray();
        log.info("Starting Azure Function Backend Function Application");
        SpringApplication.run(FunctionApplication.class , args);
    }

}
