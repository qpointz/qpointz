package io.qpointz.mill.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

public class YamlUtils {

    private YamlUtils() {
        //only static methods to be used
    }

    private static final ObjectMapper defaultYamlMapper;

    static {
        defaultYamlMapper = new ObjectMapper(new YAMLFactory());
        defaultYamlMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(new KotlinModule.Builder().build());
    }

    public static ObjectMapper defaultYamlMapper() {
        return defaultYamlMapper;
    }

}
