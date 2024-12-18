package io.qpointz.mill.services.sample.controllers;

import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.services.ServiceHandler;
import io.qpointz.mill.services.sample.services.DataService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final DataService dataService;

    @Getter
    @Setter
    private String[] captions;

    public SampleController(@Autowired ServiceHandler serviceHandler, @Autowired DataService dataService) {
        this.serviceHandler = serviceHandler;
        this.dataService = dataService;
    }

    @GetMapping("schemas")
    public List<String> listSchemas() {
        return serviceHandler.data().listSchemas(ListSchemasRequest.getDefaultInstance()).getSchemasList();
    }

    @GetMapping("captions")
    public List<String> listCaptions() {
        return Arrays.asList(this.captions);
    }

    @GetMapping("authinfo")
    public String authInfo() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("data/{schemaName}/{tableName}")
    public List<Object> dataExtract(@PathVariable("schemaName") String schemaName,
                                    @PathVariable("tableName") String tableName) {
        return this.dataService.listOfVals(schemaName, tableName);
    }




}
