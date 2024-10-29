package io.qpointz.mill.services.metainfo.controllers;

import io.qpointz.mill.services.meta.ApplicationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
@ConditionalOnProperty(prefix="mill.services.meta", name = "enable")
public class ApplicationDescriptorController {

    private final ApplicationDescriptor applicationDescriptor;

    @Bean
    @Order(100)
    @ConditionalOnProperty(name="mill.security.enable")
    SecurityFilterChain permitAllApplicationDescriptorEndpoints(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> {
            authHttp.requestMatchers("/.well-known/**").permitAll();
        }).build();
    }

    public ApplicationDescriptorController(@Autowired ApplicationDescriptor metaInfo) {
        this.applicationDescriptor = metaInfo;
    }

    @GetMapping("mill")
    public ApplicationDescriptor getInfo() {
        return this.applicationDescriptor;
    }

}
