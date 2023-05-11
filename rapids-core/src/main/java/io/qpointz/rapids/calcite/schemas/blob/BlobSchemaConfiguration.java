package io.qpointz.rapids.calcite.schemas.blob;

import lombok.*;

@Builder(access =AccessLevel.PRIVATE , builderMethodName = "privateBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlobSchemaConfiguration {

    @Getter
    private final BlobFormatFactory formatFactory;

    @Getter
    private final BlobSourceFactory sourceFactory;

    public BlobSchemaConfigurationBuilder builder(BlobSourceFactory sourceFactory, BlobFormatFactory formatFactory) {
        return BlobSchemaConfiguration.privateBuilder()
                .formatFactory(formatFactory)
                .sourceFactory(sourceFactory);
    }
}
