package io.qpointz.rapids.filesystem;

import io.qpointz.rapids.parcels.filesystem.FileSystemParcelUtils;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FileSystemAdapterBaseTest {

    protected abstract String getPartitionedHierarchicalPath();

    protected abstract FileSystemAdapter getFileSystemAdapter();

    @Test
    void traversePathes() throws IOException {
        var list = new ArrayList<Path>();
        var traversePath = this.getFileSystemAdapter().getFileSytem().getPath(this.getPartitionedHierarchicalPath());
        FileSystemParcelUtils.traverse(traversePath, p -> list.add(p));
        assertNotNull(list);
        assertTrue(list.size()>1);
    }


}