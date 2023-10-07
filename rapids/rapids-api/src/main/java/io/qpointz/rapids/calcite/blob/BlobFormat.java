package io.qpointz.rapids.calcite.blob;

import java.util.Set;

public interface BlobFormat {

    BlobTable getBlobTable(String tableName, Set<String> partitionAttributes);

}
