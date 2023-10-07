package io.qpointz.rapids.calcite.schemas.blob.impl;

import io.qpointz.rapids.calcite.blob.BlobToTableMapper;

public class TestBlobToTableMapper {

    private TestBlobToTableMapper() {

    }
    public static BlobToTableMapper create(String rx, String datasetGroup) {
        return new TestRegexBlobToTableMapper(rx, datasetGroup);
    }

}
