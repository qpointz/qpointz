package io.qpointz.mill.azure.functions;

import io.qpointz.mill.security.configuration.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@AutoConfigureBefore(SecurityConfig.class)
public class FunctionContainerConfig {

    @Bean("functionContextFlag")
    public Object functionContextFlag() {
        log.info("functionContextFlag configuration");
        return "FunctionFalg";
    }

}
