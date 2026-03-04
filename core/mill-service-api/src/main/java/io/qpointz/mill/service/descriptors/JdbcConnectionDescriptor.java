package io.qpointz.mill.service.descriptors;

import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;

import java.util.Map;

public record JdbcConnectionDescriptor(String url, AuthenticationMethodDescriptor authentication, Map<String, Object> properties) implements Descriptor {




}
