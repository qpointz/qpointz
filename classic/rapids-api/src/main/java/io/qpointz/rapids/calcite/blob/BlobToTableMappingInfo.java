package io.qpointz.rapids.calcite.blob;
import java.util.Map;

public record BlobToTableMappingInfo(String tableName, Map<String, Object> partitionValues) {
    public static BlobToTableMappingInfo create(String tableName) {
        return new BlobToTableMappingInfo(tableName, Map.of());
    }

    public static BlobToTableMappingInfo create(String tableName, Map<String, Object> partitionValues) {
        return new BlobToTableMappingInfo(tableName, partitionValues);
    }
}


