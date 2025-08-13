package io.qpointz.flow.io;

import java.net.URI;

public interface BlobPath {
    URI getUri();
    Blob blob();
}
