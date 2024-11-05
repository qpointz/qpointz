package io.qpointz.mill.services.sample.controllers;

import io.qpointz.mill.services.ServiceHandler;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/sample")
@Configuration
@ConfigurationProperties(prefix = "mill.services.sample")
public class SampleController {

    private final ServiceHandler serviceHandler;

    @Getter
    @Setter
    private String[] captions;

    public SampleController(@Autowired ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @GetMapping("schemas")
    public List<String> listSchemas() {
        return serviceHandler.listSchemas().getSchemasList();
    }

    @GetMapping("captions")
    public List<String> listCaptions() {
        return Arrays.asList(this.captions);
    }

    @GetMapping("authinfo")
    public String authInfo() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }



}
