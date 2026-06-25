package io.qpointz.mill.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlMultiDocumentReaderTest {

    @Test
    void shouldReadMultipleDocuments_fromStream() throws Exception {
        String yaml = """
            kind: AgentProfile
            id: one
            capabilities:
              - conversation
            ---
            kind: AgentProfile
            id: two
            capabilities:
              - demo
            """;
        List<Map<String, Object>> docs = YamlMultiDocumentReader.readRootMaps(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals(2, docs.size());
        assertEquals("one", docs.get(0).get("id"));
        assertEquals("two", docs.get(1).get("id"));
    }

    @Test
    void shouldReturnMultipleRoots_whenUnknownKindsPresent() throws Exception {
        String yaml = """
            kind: MetadataScope
            scopeUrn: urn:mill/metadata/scope:global
            ---
            kind: AgentProfile
            id: temp
            capabilities:
              - conversation
            """;
        List<Map<String, Object>> docs = YamlMultiDocumentReader.readRootMaps(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        assertEquals(2, docs.size());
    }
}
