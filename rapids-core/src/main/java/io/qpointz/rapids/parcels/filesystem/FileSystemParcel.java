package io.qpointz.rapids.parcels.filesystem;

import io.qpointz.rapids.parcels.Parcel;
import io.qpointz.rapids.parcels.ParcelTable;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
@Slf4j
public class FileSystemParcel implements Parcel {

    @Getter
    final private String name;

    @Getter
    final private FileSystem fileSystem;

    @Getter
    final private String parcelRootPath;

    @Getter
    private FileSystemParcelState state;

    @Getter
    private TableRegexPartitionMatcher partitionMatcher;


    @Override
    public void init() {
        log.info("{} parcel inits", this.getName());
        this.refresh();
    }

    @Override
    public void refresh() {
        log.info("{} parcel refreshes.", this.getName());
        this.state = buildState();
    }

    private FileSystemParcelState buildState() {
        log.info("{} parcel scanning for partitions", this.getName());
        var stateBuilder = FileSystemParcelState.builder();
        stateBuilder.parcelName(this.getName());

        var partitions = new ArrayList<Optional<TablePartitionInfo>>();
        FileSystemParcelUtils.traverse(
                this.getFileSystem().getPath(this.parcelRootPath),
                p-> partitions.add(this.getPartitionMatcher().match(p.toUri())));
        var allPartitions = partitions.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("{} parcel following partitions detected", this.getName());
        allPartitions
                .forEach(p-> log.info("{}-{}", this.getName(), p));
        stateBuilder.partitions(allPartitions);

        return stateBuilder.build();
    }

    @Override
    public Map<String, ParcelTable> getTables() {
        var tables = new HashMap<String, ParcelTable>();
        for (var tableName: this.getState().tableNames() ) {
            var table = new FileSystemParcelTable(this);
            tables.put(tableName, table);
        }
        return tables;
    }
}
