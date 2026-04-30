package io.qpointz.mill.utils;

import tools.jackson.dataformat.yaml.YAMLMapper;

public class YamlUtils {

    private YamlUtils() {
        //only static methods to be used
    }

    private static final YAMLMapper defaultYamlMapper;

    static {
        defaultYamlMapper = YAMLMapper.builder().build();
    }

    /**
     * Shared YAML {@link YAMLMapper} for Mill YAML payloads.
     */
    public static YAMLMapper defaultYamlMapper() {
        return defaultYamlMapper;
    }

}
