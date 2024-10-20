package io.qpointz.mill.services;
import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.configuration.MillServiceConfiguration;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;


@SpringBootApplication
@ComponentScan(basePackages = "io.qpointz.mill")
public class CalciteMillService {

    public static void main(String[] args) {
//        val configuration = MillServiceConfiguration
//                .newConfiguration()
//                .withDefaults()
//                //.withProviders()
//                .enableSecurity();
        SpringApplication.run(CalciteMillService.class ,args);
    }
}