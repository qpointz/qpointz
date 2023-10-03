package io.qpointz.rapids.calcite.blob;

import java.util.Optional;

public interface BlobToTableMapper {
    Optional<BlobToTableMappingInfo> mapPathToTable(BlobPath path);
}
