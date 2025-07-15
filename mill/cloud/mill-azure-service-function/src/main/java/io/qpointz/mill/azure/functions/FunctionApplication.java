package io.qpointz.mill.azure.functions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = "io.qpointz.mill")
@Import({BackendFunctions.class, AzureBackendFunctions.class})
@Slf4j
public class FunctionApplication {

    public static void main(String[] args) {
        log.info("Starting Azure Function Backend Function Application");
        SpringApplication.run(FunctionApplication.class , args);
    }

}
