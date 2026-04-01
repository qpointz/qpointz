package io.qpointz.mill.autoconfigure.data.schema;

import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Default relational {@link MetadataEntityUrnCodec} for catalog paths.
 * Registered here so {@code mill-metadata-autoconfigure} can depend only on compile-only schema types.
 */
@AutoConfiguration
@ConditionalOnClass(MetadataEntityUrnCodec.class)
public class MetadataEntityUrnCodecAutoConfiguration {

    /** @return canonical schema/table/column URN codec */
    @Bean
    @ConditionalOnMissingBean(MetadataEntityUrnCodec.class)
    public MetadataEntityUrnCodec metadataEntityUrnCodec() {
        return new DefaultMetadataEntityUrnCodec();
    }
}
