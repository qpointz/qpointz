package io.qpointz.rapids.calcite.schemas.blob.impl;

import io.qpointz.rapids.calcite.blob.BlobPath;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Stream;

@AllArgsConstructor
public
class TestBlobPath implements BlobPath {

    private final static String SCHEMA = "testSchema";
    private final static String SOURCE = "testSource";

    @Getter
    private final String schema;

    @Getter
    private final String sourceName;

    @Getter
    private final String path;

    public static TestBlobPath of(String path) {
        return new TestBlobPath(SCHEMA, SOURCE, path);
    }

    public static Stream<BlobPath> of(String... paths) {
        return Arrays.stream(paths)
                .map(TestBlobPath::of);
    }

}


