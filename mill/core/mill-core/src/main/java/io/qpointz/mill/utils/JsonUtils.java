package io.qpointz.mill.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {

    private JsonUtils() {
        //only static methods to be used
    }

    private static final ObjectMapper defaultJsonMapper;

    static {
        defaultJsonMapper = new ObjectMapper();
        defaultJsonMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());

    }

    public static ObjectMapper defaultJsonMapper() {
        return defaultJsonMapper;
    }
}
