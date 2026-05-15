package io.qpointz.mill.autoconfigure.data.resource;

import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import io.qpointz.mill.data.backend.resource.ResourceLocations;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Adapts Spring {@link ResourceLoader} to {@link BackendResourceLoader} for flow and other
 * data-plane reads. Bare paths without an RFC 3986 scheme are treated as local files.
 */
@RequiredArgsConstructor
public class SpringResourceLoaderBackendResourceLoader implements BackendResourceLoader {

    private final ResourceLoader resourceLoader;

    @Override
    public InputStream open(String location) throws IOException {
        var trimmed = location == null ? "" : location.trim();
        if (trimmed.isEmpty()) {
            throw new FileNotFoundException("Empty resource location");
        }
        Resource resource;
        try {
            resource = resolveResource(trimmed);
        } catch (RuntimeException e) {
            throw new IOException("Failed to resolve location: " + sanitizeDescription(trimmed), e);
        }
        if (!resource.exists()) {
            throw new FileNotFoundException("Resource not found: " + displayLocation(trimmed));
        }
        try {
            return resource.getInputStream();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("Failed to open location: " + displayLocation(trimmed), e);
        }
    }

    @Override
    public String displayLocation(String location) {
        var trimmed = location == null ? "" : location.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        try {
            return sanitizeDescription(resolveResource(trimmed).getDescription());
        } catch (RuntimeException e) {
            return sanitizeDescription(trimmed);
        }
    }

    private Resource resolveResource(String trimmed) {
        if (!ResourceLocations.hasUriScheme(trimmed)) {
            return new FileSystemResource(Path.of(trimmed));
        }
        return resourceLoader.getResource(trimmed);
    }

    private static String sanitizeDescription(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        var s = raw;
        var q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        var schemeSep = s.indexOf("://");
        if (schemeSep < 0) {
            return s;
        }
        var restStart = schemeSep + 3;
        var at = s.indexOf('@', restStart);
        if (at < 0) {
            return s;
        }
        return s.substring(0, schemeSep + 3) + s.substring(at + 1);
    }
}
