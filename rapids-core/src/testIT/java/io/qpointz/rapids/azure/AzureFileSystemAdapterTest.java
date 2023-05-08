package io.qpointz.rapids.azure;

import io.qpointz.rapids.filesystem.FileSystemAdapter;
import io.qpointz.rapids.filesystem.FileSystemAdapterBaseTest;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AzureFileSystemAdapterTest extends FileSystemAdapterBaseTest {

    private final AzureFileSystemAdapter adapter;
    private final String traversePath;

    public AzureFileSystemAdapterTest() throws IOException {
        this.adapter = AzureFileSystemAdapter.create(AzureUtils.storageAccountName, AzureUtils.storageAccountKey, AzureUtils.itContainerModels, "models/formats");
        this.traversePath = "models/formats/parquet/partitioned-hierarchy";
    }

    @Override
    protected String getPartitionedHierarchicalPath() {
        return this.traversePath;
    }

    @Override
    protected FileSystemAdapter getFileSystemAdapter() {
        return this.adapter;
    }

    @Test
    void createAdapter() throws IOException {
        var fs = AzureFileSystemAdapter.create(AzureUtils.storageAccountName, AzureUtils.storageAccountKey, AzureUtils.itContainerModels, "models");
        var p = fs.getFileSytem().getPath("models", "formats", "parquet", "airlines");
        var files = new ArrayList<Path>();
        Files.newDirectoryStream(p)
                .forEach(files::add);
        assertTrue(files.size()>0);
        var hasDir = false;
        var hasFile = false;
        for (var p1: files) {
            if (Files.isDirectory(p1)) {
                hasDir=true;
            } else {
                hasFile=true;
            }
        }
        assertTrue(hasFile, "shouldHaveFiles");
        assertFalse(hasDir, "shoud not Have Subdirs");
    }





}