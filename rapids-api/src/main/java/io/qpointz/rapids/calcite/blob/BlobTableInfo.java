package io.qpointz.rapids.calcite.blob;

import java.util.Set;

public record BlobTableInfo(String tableName, Set<String> partitionAttributes) {

}
