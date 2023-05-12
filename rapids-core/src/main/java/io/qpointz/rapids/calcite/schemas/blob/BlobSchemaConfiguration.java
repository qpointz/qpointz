package io.qpointz.rapids.calcite.schemas.blob;

import io.qpointz.rapids.calcite.blob.BlobFormatFactory;
import io.qpointz.rapids.calcite.blob.BlobSourceFactory;
import io.qpointz.rapids.calcite.blob.BlobToTableMapper;
import lombok.*;

@Builder(access =AccessLevel.PRIVATE , builderMethodName = "privateBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlobSchemaConfiguration {

    @Getter
    private final BlobFormatFactory formatFactory;

    @Getter
    private final BlobSourceFactory sourceFactory;

    @Getter
    private final BlobToTableMapper tableMapper;

    public BlobSchemaConfigurationBuilder builder(BlobSourceFactory sourceFactory, BlobFormatFactory formatFactory) {
        return BlobSchemaConfiguration.privateBuilder()
                .formatFactory(formatFactory)
                .sourceFactory(sourceFactory);
    }
}
