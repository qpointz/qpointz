package io.qpointz.rapids.calcite.schemas.blob;

import io.qpointz.rapids.calcite.blob.BlobToTableMapper;
import io.qpointz.rapids.calcite.schemas.blob.impl.TestBlobPath;
import io.qpointz.rapids.calcite.schemas.blob.impl.TestBlobToTableMapper;
import io.qpointz.rapids.calcite.schemas.blob.impl.TestRegexBlobToTableMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BlobScannerTest {

    @Test
    void mapToTable() {
        //Given
        var blobs = TestBlobPath.of("/a/table1.file", "/a/table2.file", "/a/table3.file");
        var mapper = TestBlobToTableMapper.create(".*\\/(?<table>[^\\/]+)\\.file$", "table");

        //when
        var paths = BlobScanner.mapToTable(blobs, mapper).toList();
        assertNotNull(paths);
        assertTrue(paths.size()==3);

    }
}