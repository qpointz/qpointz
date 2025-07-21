package io.qpointz.flow.io;

import java.util.Map;

public record BlobMatch(BlobPath path, Map<String,Object> matchMetadata) {
}
