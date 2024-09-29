package io.qpointz.mill.azure.functions;

import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceProvidersConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceHandlerConfiguration;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class FunctionApplication {

    public static void main(String[] args) {
        val arrayList = List.of(
                FunctionApplication.class,
                BackendFunctions.class,
                CalciteServiceProvidersConfiguration.class,
                CalciteServiceProperties.class,
                CalciteServiceConfiguration.class,
                DefaultServiceHandlerConfiguration.class
        );
        System.out.println("Hello World!");
        SpringApplication.run(arrayList.toArray(new Class<?>[0]), args);
    }

}
