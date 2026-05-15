package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import io.qpointz.mill.data.backend.resource.ResourceLocations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link BackendResourceLoader} that resolves only a fixed set of local filesystem paths, keyed by
 * their normalized {@code file:} URI strings. Used for tests and for the {@link MultiFileSourceRepository}
 * {@link Path}-based compatibility constructors.
 */
public final class LocalPathBackendResourceLoader implements BackendResourceLoader {

    private final Map<String, Path> locationToPath;

    /**
     * @param paths descriptor paths to expose; each path is registered under its normalized absolute
     *              {@code file:} URI
     */
    public LocalPathBackendResourceLoader(List<Path> paths) {
        Objects.requireNonNull(paths, "paths");
        this.locationToPath = new LinkedHashMap<>();
        for (var p : paths) {
            var abs = p.toAbsolutePath().normalize();
            var uri = abs.toUri().toString();
            locationToPath.put(uri, abs);
            locationToPath.put(abs.toString(), abs);
        }
    }

    @Override
    public InputStream open(String location) throws IOException {
        var path = resolvePath(location);
        if (path == null || !Files.isRegularFile(path)) {
            throw new FileNotFoundException("Resource not found: " + displayLocation(location));
        }
        return Files.newInputStream(path);
    }

    @Override
    public String displayLocation(String location) {
        if (location == null) {
            return "";
        }
        var path = resolvePath(location.trim());
        return path != null ? path.toString() : location.trim();
    }

    private Path resolvePath(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        var trimmed = location.trim();
        var direct = locationToPath.get(trimmed);
        if (direct != null) {
            return direct;
        }
        if (!ResourceLocations.hasUriScheme(trimmed)) {
            return locationToPath.get(Path.of(trimmed).toAbsolutePath().normalize().toUri().toString());
        }
        try {
            var uri = java.net.URI.create(trimmed);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                var p = Path.of(uri);
                return locationToPath.get(p.toAbsolutePath().normalize().toUri().toString());
            }
        } catch (Exception ignored) {
            // fall through
        }
        return locationToPath.get(trimmed);
    }
}
