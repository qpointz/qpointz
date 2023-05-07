package io.qpointz.rapids.formats.parquet;

import com.azure.storage.blob.nio.AzureFileSystem;
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
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;

@Slf4j
public class RapidsParquetSchemaFactory implements SchemaFactory {

    private static final String AZ_STORAGE_ACCOUNT_NAME = "az.storage.account.name";
    private static final String AZ_STORAGE_ACCOUNT_KEY = "az.storage.account.key";
    private static final String AZ_STORAGE_CONTAINERS = "az.storage.container";
    private static String DIR_KEY = "rootDir";

    private static String CACHE_EXPIRES_AFTER = "cache.expiresAfter";

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
        var cahceExpiresAfter = 300;

        if (operand.containsKey(CACHE_EXPIRES_AFTER)) {
            cahceExpiresAfter = Integer.parseInt(operand.get(CACHE_EXPIRES_AFTER).toString());
        }
        log.info("Schema caching set to {} seconds", cahceExpiresAfter);

        FileSystemAdapter fileSysteAdapter = null;
        try {
            fileSysteAdapter = getFileSystemAdapter(operand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new RapidsParquetSchema(fileSysteAdapter, pattern, datasetGroup, cahceExpiresAfter);
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

        if (!operand.containsKey(AZ_STORAGE_ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Missing ADLS storage account name. Provide '%s' parameter".formatted(AZ_STORAGE_ACCOUNT_NAME));
        }

        if (!operand.containsKey(AZ_STORAGE_ACCOUNT_KEY)) {
            throw new IllegalArgumentException("Missing ADLS storage account key. Provide '%s' parameter".formatted(AZ_STORAGE_ACCOUNT_KEY));
        }

        if (!operand.containsKey(AZ_STORAGE_CONTAINERS)) {
            throw new IllegalArgumentException("Missing ADLS conatiner(s). Provide '%s' parameter".formatted(AZ_STORAGE_CONTAINERS));
        }

        String accountName = operand.get(AZ_STORAGE_ACCOUNT_NAME).toString();
        String accountKey = operand.get(AZ_STORAGE_ACCOUNT_KEY).toString();
        String containerName = operand.get(AZ_STORAGE_CONTAINERS).toString();

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
