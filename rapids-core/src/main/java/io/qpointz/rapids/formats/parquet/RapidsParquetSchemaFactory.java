package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.azure.AzureFileSystemAdapter;
import io.qpointz.rapids.filesystem.FileSystemAdapter;
import io.qpointz.rapids.filesystem.LocalFileSystemAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class RapidsParquetSchemaFactory implements SchemaFactory {

    private static String DIR_KEY = "rootDir";
    private static String RX_DATASET_GROUP_KEY = "rx.datasetGroup";

    private static String RX_PATTERN_KEY = "rx.pattern";

    private static String FS_TYPE = "fs.type";

    public RapidsParquetSchemaFactory() {

    }

    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {

        if (!operand.containsKey(RX_DATASET_GROUP_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(RX_DATASET_GROUP_KEY));
        }

        if (!operand.containsKey(RX_PATTERN_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(RX_PATTERN_KEY));
        }

        var pattern = operand.get(RX_PATTERN_KEY).toString();
        var datasetGroup = operand.get(RX_DATASET_GROUP_KEY).toString();

        FileSystemAdapter fileSysteAdapter = null;
        try {
            fileSysteAdapter = getFileSystemAdapter(operand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new RapidsParquetSchema(fileSysteAdapter, pattern, datasetGroup);
    }

    private FileSystemAdapter getFileSystemAdapter(Map<String, Object> operand) throws IOException {
        var fstype = "local";
        if (!operand.containsKey(FS_TYPE)) {
            log.warn("No file system type specified. Fallback to  '{}' filesystem", fstype);
        } else {
            fstype = operand.get(FS_TYPE).toString();
        }

        switch (fstype) {
            case "local":
                return createLocalFileSystemSystemAdapter(operand);
            case "az":
                return createAzureDataLakeAdapter(operand);
            default:
                throw new IllegalArgumentException("Unknown file system type '%s'".formatted(fstype));
        }
    }

    private FileSystemAdapter createAzureDataLakeAdapter(Map<String, Object> operand) throws IOException {
        var rootPath = operand.get(DIR_KEY).toString();
        if (!operand.containsKey(DIR_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(DIR_KEY));
        }
        String accountName = null;
        String accountKey = null;
        String containerName = null;
        return AzureFileSystemAdapter.create(accountName, accountKey, containerName, rootPath);
    }

    private FileSystemAdapter createLocalFileSystemSystemAdapter(Map<String, Object> operand) {
        var rootPath = operand.get(DIR_KEY).toString();
        if (!operand.containsKey(DIR_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(DIR_KEY));
        }
        return new LocalFileSystemAdapter(Paths.get(rootPath));
    }
}
