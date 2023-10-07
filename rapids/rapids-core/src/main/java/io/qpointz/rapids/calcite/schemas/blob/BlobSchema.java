package io.qpointz.rapids.calcite.schemas.blob;

import io.qpointz.rapids.calcite.blob.BlobFormat;
import io.qpointz.rapids.calcite.blob.BlobSource;
import lombok.Getter;
import org.apache.calcite.schema.impl.AbstractSchema;


public class BlobSchema extends AbstractSchema {


    private final BlobSchemaConfiguration configuration;

    protected BlobSchema(BlobSchemaConfiguration config) {
        this.configuration = config;
    }

    @Getter(lazy = true)
    private final BlobFormat blobFormat = this.createBlobFormat();
    private BlobFormat createBlobFormat() {
        assert this.configuration.getFormatFactory() ==null : "Format factory is null";
        return this.configuration
                .getFormatFactory()
                .create();
    }

    @Getter(lazy=true)
    private final BlobSource blobSource = this.createBlobSource();
    private BlobSource createBlobSource() {
        assert this.configuration.getSourceFactory() == null : "Source factory is null";
        return this.configuration
                .getSourceFactory()
                .create();
    }

}
