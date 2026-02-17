package io.qpointz.mill.metadata.repository.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring-based implementation of {@link ResourceResolver}.
 * Supports glob patterns via {@link ResourcePatternResolver} and simple paths via {@link ResourceLoader}.
 */
public class SpringResourceResolver implements ResourceResolver {

    private final ResourceLoader resourceLoader;

    public SpringResourceResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<ResolvedResource> resolve(String locationPattern) throws IOException {
        List<ResolvedResource> result = new ArrayList<>();

        ResourcePatternResolver patternResolver = null;
        if (resourceLoader instanceof ResourcePatternResolver) {
            patternResolver = (ResourcePatternResolver) resourceLoader;
        }

        if (patternResolver != null && (locationPattern.contains("*") || locationPattern.contains("?"))) {
            Resource[] matched = patternResolver.getResources(locationPattern);
            for (Resource resource : matched) {
                if (resource.exists()) {
                    result.add(new ResolvedResource(resource.toString(), resource.getInputStream()));
                }
            }
        } else {
            Resource resource = resourceLoader.getResource(locationPattern);
            if (resource.exists()) {
                result.add(new ResolvedResource(resource.toString(), resource.getInputStream()));
            }
        }

        return result;
    }
}
