package io.qpointz.mill.metadata.io;

import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface MetadataImporter {
    Collection<MetadataEntity> importFrom(InputStream source) throws IOException;
}
