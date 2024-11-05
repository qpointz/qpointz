package io.qpointz.mill.security.annotations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationsTestConfiguration {

    @Bean
    @ConditionalOnSecurity
    @Qualifier("SecurityEnabled")
    public boolean securityEnabled() {
        return true;
    }

    @Bean
    @Qualifier("SecurityEnabled")
    @ConditionalOnSecurity(false)
    public boolean securityDisabled() {
        return false;
    }


}
