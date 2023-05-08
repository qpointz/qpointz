package io.qpointz.rapids.formats.parquet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.qpointz.rapids.filesystem.FileSystemAdapter;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class RapidsParquetSchema extends AbstractSchema {

    private final TableRegexPartitionMatcher partitionMapper;

    private final FileSystemAdapter fileSystemAdapter;
    private final Cache<String, RelDataType> relTypeCache;
    private final int cacheExpiresAfter;

    public RapidsParquetSchema(FileSystemAdapter fileSystemAdapter, String matchRegex, String datasetGroupName, int cacheExpiresAfter) {
        this.fileSystemAdapter = fileSystemAdapter;
        this.partitionMapper = TableRegexPartitionMatcher.builder()
                .regexPattern(matchRegex)
                .datasetGroup(datasetGroupName)
                .partitionValueGroups(Map.of())
                .build();

        this.cacheExpiresAfter = cacheExpiresAfter;

        this.relTypeCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(this.cacheExpiresAfter, TimeUnit.SECONDS)
                .build();
    }


    private Stream<Pair<Path, Optional<TablePartitionInfo>>> listPartitionsFiles() {
        var partitions = new ArrayList<Pair<Path, Optional<TablePartitionInfo>>>();
        try {
            FileSystemParcelUtils.traverse(
                        this.fileSystemAdapter.getTraverseRoot(),
                        p-> partitions.add(new Pair<>(p, this.partitionMapper.match(p.toUri()))));
        } catch (IOException e) {
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
        try {
            return this.relTypeCache
                    .get(tableName, () -> loadRowType(tableName, typeFactory));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public RelDataType loadRowType(String tableName, RelDataTypeFactory typeFactory) throws IOException {
        var mayBeFirst = this.getTablePaths(tableName).stream().findAny();

        if (mayBeFirst.isEmpty()) {
            throw new IllegalStateException("No files found for %s dataset".formatted(tableName));
        }
        var path = mayBeFirst.get();

        ParquetReader<GenericRecord> reader = getParquetReader(path);
        var topRecord = reader.read();
        var schema = topRecord.getSchema();
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
        var file = new InputFile() {

            @Override
            public long getLength() {
                try {
                    return Files.newByteChannel(path).size();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public SeekableInputStream newStream() throws IOException {
                var channel = Files.newByteChannel(path);
                return new SeekableInputStream() {
                    @Override
                    public int read() throws IOException {
                        var bf = ByteBuffer.allocate(1);
                        var bytesRead = channel.read(bf);
                        if (bytesRead == 0) {
                            return -1;
                        } else {
                            return Byte.toUnsignedInt(bf.get(0));
                        }
                    }

                    @Override
                    public long getPos() throws IOException {
                        return channel.size();
                    }

                    @Override
                    public void seek(long newPos) throws IOException {
                        channel.position(newPos);
                    }

                    @Override
                    public void readFully(byte[] bytes) throws IOException {
                        readFully(bytes, 0, bytes.length);
                    }

                    @Override
                    public void readFully(byte[] bytes, int start, int len) throws IOException {
                        channel.position(0);
                        var byteBuffer = ByteBuffer.wrap(bytes, start, len);
                        channel.read(byteBuffer);
                    }

                    @Override
                    public int read(ByteBuffer buf) throws IOException {
                        return channel.read(buf);
                    }

                    @Override
                    public void readFully(ByteBuffer buf) throws IOException {
                        channel.read(buf);
                    }

                };
            }
        };

        return AvroParquetReader.<GenericRecord>builder(file).build();
    }



}
