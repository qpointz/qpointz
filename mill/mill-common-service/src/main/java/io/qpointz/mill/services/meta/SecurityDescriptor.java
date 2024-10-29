package io.qpointz.mill.services.meta;

import io.qpointz.mill.security.authentication.AuthenticationType;

import java.util.Set;

public record SecurityDescriptor(boolean enabled, Set<AuthenticationType> authMethods) {
}