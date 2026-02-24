package io.qpointz.mill.service.controllers;

import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ApplicationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
@ConditionalOnService("meta")
public class ApplicationDescriptorController {

    private final ApplicationDescriptor applicationDescriptor;

    public ApplicationDescriptorController(@Autowired ApplicationDescriptor metaInfo) {
        this.applicationDescriptor = metaInfo;
    }

    @GetMapping("mill")
    public ApplicationDescriptor getInfo() {
        return this.applicationDescriptor;
    }
}
