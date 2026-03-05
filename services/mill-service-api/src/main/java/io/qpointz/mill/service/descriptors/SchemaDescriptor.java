package io.qpointz.mill.service.descriptors;

import java.net.URI;

public record SchemaDescriptor(String name, URI link) implements Descriptor {}
