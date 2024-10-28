package io.qpointz.mill;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "io.qpointz")
public class CalciteMillService {

    public static void main(String[] args) {
        SpringApplication.run(CalciteMillService.class ,args);
    }
}