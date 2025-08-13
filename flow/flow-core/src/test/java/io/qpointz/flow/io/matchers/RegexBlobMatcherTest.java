package io.qpointz.flow.io.matchers;

import io.qpointz.flow.io.BlobPath;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegexBlobMatcherTest {

    @Test
    void trivia() {
        val bp = mock(BlobPath.class);
        when(bp.getUri()).thenReturn(URI.create("/a/b/c/table.file"));
        val matcher = new RegexBlobMatcher(".*\\/(\\w+)\\.file$", Map.of(1, "table"));
        val result = matcher.match(bp);
        assertTrue(result.isPresent());
        val match = result.get();
        assertEquals("/a/b/c/table.file", match.matchMetadata().get("blob-match"));
        assertEquals("table", match.matchMetadata().get("table"));
    }


}