package io.qpointz.rapids.providers.local.blob;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LocalFilesystemBlobSourceTest {

    @Test
    void listBlobs() throws IOException {
        var fls = new LocalFilesystemBlobSource(Paths.get("../rapids-example/data/partitioned-hierarchy/2023"));
        var blobs = fls.listBlobs().toList();
        assertTrue(blobs.size()>0);
    }

    @Test
    void openSeekableChannel() throws IOException {
        var fls = new LocalFilesystemBlobSource(Paths.get("../rapids-example/data/partitioned-hierarchy/2023"));
        var blob = fls.listBlobs().toList().get(0);
        var ch = fls.openSeekableChannel(blob);
        assertTrue(ch.size()>0);
    }

}