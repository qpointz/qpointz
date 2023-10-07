package io.qpointz.rapids.parcels.filesystem;

import java.net.URI;
import java.util.Optional;

public interface TablePartitionInfoMatcher {

    Optional<TablePartitionInfo> match(URI uri);

}
