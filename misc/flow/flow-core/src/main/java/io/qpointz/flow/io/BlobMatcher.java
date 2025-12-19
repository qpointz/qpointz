package io.qpointz.flow.io;

import java.util.Optional;

public interface BlobMatcher {
    Optional<BlobMatch> match(BlobPath path);
}
