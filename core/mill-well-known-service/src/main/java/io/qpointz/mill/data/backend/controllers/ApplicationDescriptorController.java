package io.qpointz.mill.data.backend.controllers;

import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import io.qpointz.mill.data.backend.descriptors.ApplicationDescriptor;
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
