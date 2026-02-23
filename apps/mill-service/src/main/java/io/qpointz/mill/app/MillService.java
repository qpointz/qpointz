package io.qpointz.mill.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "io.qpointz")
@EntityScan(basePackages = "io.qpointz")
@EnableScheduling
public class MillService {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{MillService.class} ,args);
    }

}
