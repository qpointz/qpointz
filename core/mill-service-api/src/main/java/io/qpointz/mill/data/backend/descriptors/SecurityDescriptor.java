package io.qpointz.mill.data.backend.descriptors;

import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;

import java.util.Collection;

public record SecurityDescriptor(boolean enabled, Collection<AuthenticationMethodDescriptor> authMethods) {
}
