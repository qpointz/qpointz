package io.qpointz.flow.io;

import java.util.Collection;

public interface BlobSource {
    Collection<BlobPath> listBlobs() throws Exception;
}
