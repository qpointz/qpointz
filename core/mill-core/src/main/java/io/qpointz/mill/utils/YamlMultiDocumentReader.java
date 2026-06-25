package io.qpointz.mill.utils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads multi-document YAML streams via {@link YAMLMapper#readValues(JsonParser, Class)}.
 *
 * <p>Each document is parsed as a {@code Map<String, Object>}. Empty maps and comment-only
 * fragments are skipped.
 */
public final class YamlMultiDocumentReader {

    private YamlMultiDocumentReader() {
    }

    /**
     * @param input YAML bytes (one or more {@code ---}-separated documents)
     * @return root maps in file order
     * @throws IOException when parsing fails
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readRootMaps(InputStream input) throws IOException {
        YAMLMapper mapper = YamlUtils.defaultYamlMapper();
        List<Map<String, Object>> docs = new ArrayList<>();
        try (JsonParser parser = mapper.createParser(input);
             MappingIterator<Map> it = mapper.readValues(parser, Map.class)) {
            while (it.hasNext()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doc = it.next();
                if (doc == null || doc.isEmpty()) {
                    continue;
                }
                docs.add(new LinkedHashMap<>(doc));
            }
        }
        return docs;
    }
}
