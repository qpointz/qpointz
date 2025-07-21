package io.qpointz.flow.io.local;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemBlobSourceTest {

    @Test
    void triviaTraversal() throws Exception {
        val src = partitionedTest();
        assertTrue(src.listBlobs().size()>0);
    }

    private FileSystemBlobSource partitionedTest() {
        return new FileSystemBlobSource(Paths.get("../../test/datasets/partitioned"));
    }

    @Test
    void urisAreRelative() throws Exception {
        val f = partitionedTest()
                .listBlobs()
                .stream()
                .findAny()
                .get();
        assertTrue(f.getUri().toString().startsWith("file:"), "Path starts with schema");
        assertTrue(f.getUri().isAbsolute(), "Path is absolute");
    }

    @Test
    void sourceReturnsFilesOnly() throws Exception {
        assertTrue(partitionedTest().listBlobs().stream()
                .filter(k-> Files.isDirectory(Paths.get(k.getUri())))
                .findFirst()
                .isEmpty()
        );
    }

}