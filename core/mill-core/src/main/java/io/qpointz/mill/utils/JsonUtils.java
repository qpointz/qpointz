package io.qpointz.mill.utils;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.kotlin.KotlinModule;

public class JsonUtils {

    private JsonUtils() {
        //only static methods to be used
    }

    private static final JsonMapper defaultJsonMapper;

    static {
        defaultJsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .addModule(new KotlinModule.Builder().build())
                .build();
    }

    /**
     * Shared JSON {@link JsonMapper} with Kotlin support and classpath-discovered Jackson modules.
     */
    public static JsonMapper defaultJsonMapper() {
        return defaultJsonMapper;
    }

}
