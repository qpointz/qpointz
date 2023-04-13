package io.qpointz.rapids.parcels.filesystem;

import io.quarkus.arc.All;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.linq4j.Enumerable;

import java.util.Collection;
import java.util.List;

@Builder
@Slf4j
@All
public class FileSystemParcelState {

    @Getter
    private final List<TablePartitionInfo> partitions;

    @Getter
    private final String parcelName;

    public Collection<String> tableNames() {
        return this.getPartitions().stream()
                .map(TablePartitionInfo::getTableName)
                .distinct()
                .toList();
    }

}
