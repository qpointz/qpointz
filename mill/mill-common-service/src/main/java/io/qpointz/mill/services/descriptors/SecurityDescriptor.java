package io.qpointz.mill.services.descriptors;

import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
import io.qpointz.mill.security.authentication.AuthenticationType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record SecurityDescriptor(boolean enabled, Collection<AuthenticationMethodDescriptor> authMethods) {
}