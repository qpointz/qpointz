package io.qpointz.rapids.calcite.schemas.blob;

import io.qpointz.rapids.calcite.blob.BlobPath;
import io.qpointz.rapids.calcite.blob.BlobToTableMapper;
import io.qpointz.rapids.calcite.blob.BlobToTableMappingInfo;

import java.util.*;
import java.util.stream.Stream;
import static java.util.stream.Collectors.*;

public class BlobScanner {

    public record PartitionInfo(Map<String,Object> partition, List<BlobPath> paths) {

    }

    public record TableInfo(String tableName, List<PartitionInfo> partitions) {

    }

    public static Stream<BlobToTableMappingInfo> mapToTable(Stream<BlobPath> blobs, BlobToTableMapper mapper) {
        return blobs
                .map(mapper::mapPathToTable)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

//    public static Collection<TableInfo> tableInfos(Stream<BlobPath> blobs, BlobToTableMapper mapper) {
//        final var byTableName = BlobScanner
//                .mapToTable(blobs, mapper)
//                .collect(groupingBy(p-> p.tableName(), toList()));
//        var tableInfos = new ArrayList<TableInfo>();
//        for (var es : byTableName.entrySet()) {
//            tableInfos.add(createTableInfo(es.getKey(), es.getValue()));
//        }
//        return tableInfos;
//    }

//    private static TableInfo createTableInfo(String key, List<BlobToTableMappingInfo> value) {
//        final var byPartitions = value.stream().collect(groupingBy(p-> p.partitionValues(), toList()));
//        return new TableInfo();
//    }


}
