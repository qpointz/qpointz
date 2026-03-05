package io.qpointz.mill.service.descriptors;

import java.util.Map;

public record JdbcConnectionDescriptor(String url, AuthMethodDescriptor auth, Map<String, Object> properties) implements Descriptor {




}
