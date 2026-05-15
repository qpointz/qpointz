package io.qpointz.mill.data.backend.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring-free entry point for reading backend configuration bytes from a location string.
 *
 * <p>Implementations interpret Mill resource locations such as {@code classpath:}, {@code file:},
 * and cloud schemes registered on the application resource loader in Spring Boot. Clean backend
 * modules depend only on this interface.
 */
public interface BackendResourceLoader {

    /**
     * Opens an input stream for the given location.
     *
     * @param location a Mill resource location (for example {@code classpath:flow/a.yml},
     *                 {@code file:/etc/mill/a.yml}, or a bare filesystem path treated as {@code file:})
     * @return a new stream; callers must close it
     * @throws IOException if the resource is missing, the scheme is unsupported, or I/O fails
     */
    InputStream open(String location) throws IOException;

    /**
     * Returns a log-safe description of the location for errors and diagnostics.
     *
     * <p>Must not include credentials, SAS tokens, or signed URL material.
     *
     * @param location the same style of location string as {@link #open(String)}
     * @return a non-secret display string
     */
    String displayLocation(String location);
}
