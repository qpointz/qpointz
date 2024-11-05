package io.qpointz.mill.test.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ComponentScan(basePackages = "io.qpointz")
public class MainLala {

    public static void main(String[] args) {
        SpringApplication.run(MainLala.class, args);
    }

}
