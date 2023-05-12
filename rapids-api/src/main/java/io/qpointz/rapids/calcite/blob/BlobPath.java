package io.qpointz.rapids.calcite.blob;

import java.net.URI;
import java.nio.file.Path;

public interface BlobPath {

    String getSchema();

    String getPath();

}
