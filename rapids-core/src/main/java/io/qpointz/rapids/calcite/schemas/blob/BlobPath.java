package io.qpointz.rapids.calcite.schemas.blob;

import java.net.URI;
import java.nio.file.Path;

public interface BlobPath {

    String getSchema();

    String getSourceName();

    String getPath();

}
