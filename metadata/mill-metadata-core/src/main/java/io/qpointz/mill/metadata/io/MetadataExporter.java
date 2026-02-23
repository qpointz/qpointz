package io.qpointz.mill.metadata.io;

import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface MetadataExporter {
    void export(Collection<MetadataEntity> entities, OutputStream target) throws IOException;
}
