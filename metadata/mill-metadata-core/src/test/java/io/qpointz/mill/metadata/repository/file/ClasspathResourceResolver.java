package io.qpointz.mill.metadata.repository.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure-Java classpath resource resolver for testing.
 * Supports simple classpath: prefixed locations (no glob patterns).
 */
class ClasspathResourceResolver implements ResourceResolver {

    @Override
    public List<ResolvedResource> resolve(String locationPattern) throws IOException {
        String path = locationPattern;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            return List.of();
        }

        List<ResolvedResource> result = new ArrayList<>();
        result.add(new ResolvedResource(locationPattern, is));
        return result;
    }
}
