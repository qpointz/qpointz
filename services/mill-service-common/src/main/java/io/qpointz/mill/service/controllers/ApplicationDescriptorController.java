package io.qpointz.mill.service.controllers;

import io.qpointz.mill.service.service.WellKnownService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HTTP entry for Mill discovery metadata under {@code /.well-known}.
 *
 * <p>Trailing-slash variants are registered so clients following RFC 8615-style URL joins still resolve.
 */
@RestController
@RequestMapping("/.well-known")
public class ApplicationDescriptorController {

    private final WellKnownService service;

    /**
     * @param service aggregates application, security, and contributed {@link io.qpointz.mill.service.descriptors.Descriptor}s
     */
    public ApplicationDescriptorController(WellKnownService service) {
        this.service = service;
    }

    /**
     * Returns the Mill discovery document assembled by {@link io.qpointz.mill.service.service.WellKnownService}.
     *
     * @return map suitable for JSON serialization; keys include {@code app} and {@link io.qpointz.mill.service.descriptors.Descriptor#getTypeName()} buckets
     */
    @GetMapping({"mill", "mill/", "", "/"})
    public Map<String, ?> getInfo() {
        return this.service.metaInfo();
    }

}
