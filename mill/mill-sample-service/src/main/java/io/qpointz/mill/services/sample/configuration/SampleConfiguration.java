package io.qpointz.mill.services.sample.configuration;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SampleConfiguration {

    @Bean(name = "lala")
    @Qualifier("lala")
    public int getLala() {
        return 100;
    }

}
