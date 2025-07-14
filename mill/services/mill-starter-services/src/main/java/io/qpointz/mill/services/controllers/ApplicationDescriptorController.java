package io.qpointz.mill.services.controllers;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.descriptors.ApplicationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
