package io.qpointz.mill.services;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "io.qpointz.mill")
public class CalciteMillService {

    public static void main(String[] args) {
        SpringApplication.run(CalciteMillService.class ,args);
    }
}