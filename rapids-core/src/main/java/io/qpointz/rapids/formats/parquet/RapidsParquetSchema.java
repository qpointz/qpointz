package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.parcels.filesystem.FileSystemParcelUtils;
import io.qpointz.rapids.parcels.filesystem.TablePartitionInfo;
import io.qpointz.rapids.parcels.filesystem.TableRegexPartitionMatcher;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class RapidsParquetSchema extends AbstractSchema {

    @AllArgsConstructor
    @EqualsAndHashCode
    private class Pair<A,B> {
        @Getter
        private final A left;

        @Getter
        final B right;
    }

    private final TableRegexPartitionMatcher partitionMapper;
    private final FileSystem fileSystem;
    private final Path rootPath;

    public RapidsParquetSchema(FileSystem fs, Path rootDirectory, String matchRegex, String datasetGroupName) {
        this.fileSystem = fs;
        this.partitionMapper = TableRegexPartitionMatcher.builder()
                .regexPattern(matchRegex)
                .datasetGroup(datasetGroupName)
                .partitionValueGroups(Map.of())
                .build();
        this.rootPath = rootDirectory;
    }

    private Stream<Pair<Path, Optional<TablePartitionInfo>>> listPartitionsFiles() {
        var partitions = new ArrayList<Pair<Path, Optional<TablePartitionInfo>>>();
        try {
            FileSystemParcelUtils.traverse(
                        this.rootPath.toAbsolutePath(),
                        p-> partitions.add(new Pair<>(p, this.partitionMapper.match(p.toUri()))));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return partitions.stream();
    }

    private Set<String> tableNames() {
        return Set.copyOf(listPartitionsFiles()
                .filter(p -> p.right.isPresent())
                .map(p-> p.right.get().getTableName())
                .distinct()
                .toList());
    }

     Set<Path> getTablePaths(String tableName) {
        return Set.copyOf(listPartitionsFiles()
                .filter(p->p.right.isPresent() && p.right.get().getTableName().equals(tableName))
                .map(p-> p.left)
                .toList()
        );
    }

    @Override
    protected Map<String, Table> getTableMap() {
        var tables = new HashMap<String, Table>();
        for (var tn : this.tableNames()) {
            tables.put(tn, new RapidsParquetTable(this, tn));
        }
        return tables;
    }

}
