package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.parcels.filesystem.FileSystemParcelUtils;
import io.qpointz.rapids.parcels.filesystem.TablePartitionInfo;
import io.qpointz.rapids.parcels.filesystem.TableRegexPartitionMatcher;
import io.qpointz.rapids.util.Pair;
import org.apache.avro.generic.GenericRecord;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class RapidsParquetSchema extends AbstractSchema {

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
                .filter(p -> p.getRight().isPresent())
                .map(p-> p.getRight().get().getTableName())
                .distinct()
                .toList());
    }

     Set<Path> getTablePaths(String tableName) {
        return Set.copyOf(listPartitionsFiles()
                .filter(p-> p.getRight().isPresent() && p.getRight().get().getTableName().equals(tableName))
                .map(Pair::getLeft)
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

    public RelDataType getRowType(String tableName, RelDataTypeFactory typeFactory) throws IOException {
        var mayBeFirst = this.getTablePaths(tableName).stream().findAny();

        if (mayBeFirst.isEmpty()) {
            throw new IllegalStateException("No files found for %s dataset".formatted(tableName));
        }
        var path = mayBeFirst.get();

        ParquetReader<GenericRecord> reader = getParquetReader(path);
        var record = reader.read();
        var schema = record.getSchema();
        var s = new AvroSchemaConverter().convert(schema);

        final var names = new ArrayList<String>();
        final var types = new ArrayList<RelDataType>();
        s.entrySet().forEach(k-> {
            names.add(k.getKey());
            types.add(k.getValue().asRelDataType(typeFactory));
        });

        return typeFactory.createStructType(types, names);
    }

    public ParquetReader<GenericRecord> getParquetReader(Path path) throws IOException {
        //var fins = new FileInputStream(path.toFile());
        var file = new InputFile() {

            @Override
            public long getLength() throws IOException {
                return path.toFile().length();
            }

            @Override
            public SeekableInputStream newStream() throws IOException {
                return new RapidsSeekableFileInputStream(new FileInputStream(path.toFile()));
            }
        };


        ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(file).build();
        return reader;
    }

//    public void lala(Path path) throws IOException {
//        var reader = this.getParquetReader(path);
//        var a = new Iterator<GenericRecord>() {
//
//            @Override
//            public boolean hasNext() {
//                return false;
//            }
//
//            @Override
//            public GenericRecord next() {
//                return null;
//            }
//        }
//    }


}
