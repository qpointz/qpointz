package io.qpointz.rapids.filesystem;

import java.nio.file.FileSystem;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileSystemAdapterTest  extends FileSystemAdapterBaseTest {

    private final LocalFileSystemAdapter adapter;
    private final String traversePath;

    public LocalFileSystemAdapterTest() {
        this.traversePath = "../rapids-example/data/partitioned-hierarchy";
        this.adapter = new LocalFileSystemAdapter(Paths.get(this.traversePath).toAbsolutePath());
    }

    @Override
    protected String getPartitionedHierarchicalPath() {
        return this.traversePath;
    }

    @Override
    protected FileSystemAdapter getFileSystemAdapter() {
        return this.adapter;
    }


}