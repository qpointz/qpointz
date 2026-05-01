package io.qpointz.mill.service.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Tag interface for typed entries aggregated into well-known and discovery responses.
 *
 * <p>Implementations are grouped by {@link #getTypeName()} when building JSON maps for clients.
 */
public interface Descriptor {

    /**
     * Logical type key used when grouping descriptors (for example {@link DescriptorTypes#SERVICE_TYPE_NAME},
     * {@link DescriptorTypes#SCHEMA_TYPE_NAME}).
     *
     * @return non-null type discriminator; omitted from JSON serialization where redundant
     */
    @JsonIgnore
    String getTypeName();

}
