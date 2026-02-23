package io.qpointz.mill.metadata.repository.file

import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternResolver

/** Spring-based [ResourceResolver] supporting plain paths and glob patterns. */
class SpringResourceResolver(private val resourceLoader: ResourceLoader) : ResourceResolver {

    override fun resolve(locationPattern: String): List<ResolvedResource> {
        val result = mutableListOf<ResolvedResource>()
        val patternResolver = resourceLoader as? ResourcePatternResolver

        if (patternResolver != null && (locationPattern.contains("*") || locationPattern.contains("?"))) {
            val matched = patternResolver.getResources(locationPattern)
            for (resource in matched) {
                if (resource.exists()) {
                    result.add(ResolvedResource(resource.toString(), resource.inputStream))
                }
            }
        } else {
            val resource = resourceLoader.getResource(locationPattern)
            if (resource.exists()) {
                result.add(ResolvedResource(resource.toString(), resource.inputStream))
            }
        }
        return result
    }
}
