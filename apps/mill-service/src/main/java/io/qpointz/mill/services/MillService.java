package io.qpointz.mill.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "io.qpointz")
@EntityScan(basePackages = "io.qpointz")
public class MillService {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{MillService.class} ,args);
    }

}
