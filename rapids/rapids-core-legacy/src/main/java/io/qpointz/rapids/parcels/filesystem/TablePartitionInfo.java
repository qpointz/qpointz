package io.qpointz.rapids.parcels.filesystem;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class TablePartitionInfo {

    @Getter
    private final String tableName;

    @Getter
    private final Map<String, PartitionValue> partitionMap;

}
