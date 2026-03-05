package io.qpointz.mill.service.descriptors;

import java.util.Collection;

public record SecurityDescriptor(boolean enabled, Collection<AuthMethodDescriptor> authMethods) implements Descriptor {
}
