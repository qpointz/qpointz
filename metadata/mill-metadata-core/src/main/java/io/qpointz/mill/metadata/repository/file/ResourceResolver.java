package io.qpointz.mill.metadata.repository.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Abstraction for resolving resource location patterns to input streams.
 * Decouples file-based metadata loading from any specific resource framework.
 */
@FunctionalInterface
public interface ResourceResolver {

    /**
     * Resolve a location pattern to a list of named resources.
     *
     * @param locationPattern a file path or pattern (e.g., "classpath:metadata/*.yml")
     * @return list of resolved resources; never null
     * @throws IOException if resolution fails
     */
    List<ResolvedResource> resolve(String locationPattern) throws IOException;

    /**
     * A named resource with its content as an InputStream.
     */
    record ResolvedResource(String name, InputStream inputStream) {}
}
