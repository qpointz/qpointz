package io.qpointz.mill.services.sample.controllers;

import io.qpointz.mill.services.ServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/sample")
@Configuration
public class SampleController {

    @Autowired
    ServiceHandler serviceHandler;

    @GetMapping("schemas")
    public List<String> listSchemas() {
        return serviceHandler.listSchemas().getSchemasList();
    }




}
