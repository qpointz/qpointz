package io.qpointz.mill.services.metainfo.controllers;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.meta.ApplicationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/.well-known")
@ConditionalOnService("meta")
public class ApplicationDescriptorController {

    private final ApplicationDescriptor applicationDescriptor;


    @Bean
    @ConditionalOnSecurity
    @Order(0)
    SecurityFilterChain allowAnonymousAccessToMetaService(HttpSecurity http) throws Exception {
        return http.securityMatcher("/.well-known/**")
                .authorizeHttpRequests(a ->
                    a.anyRequest().permitAll()
                ).build();
    }

    public ApplicationDescriptorController(@Autowired ApplicationDescriptor metaInfo) {
        this.applicationDescriptor = metaInfo;
    }

    @GetMapping("mill")
    public ApplicationDescriptor getInfo() {
        return this.applicationDescriptor;
    }

}
