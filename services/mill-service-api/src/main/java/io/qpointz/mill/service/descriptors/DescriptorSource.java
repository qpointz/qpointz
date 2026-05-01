package io.qpointz.mill.service.descriptors;

import java.util.Collection;

/**
 * Supplies a batch of {@link Descriptor} values under a shared group name for well-known aggregation.
 *
 * <p>Allows modules to contribute descriptors without registering each instance as its own Spring bean.
 */
public interface DescriptorSource {

    /**
     * Group label for this source's descriptors (often matches {@link Descriptor#getTypeName()} of produced
     * entries, e.g. {@link DescriptorTypes#SCHEMA_TYPE_NAME}).
     *
     * @return non-null group name
     */
    String getGroupName();

    /**
     * All descriptors contributed by this source for the current runtime configuration.
     *
     * @return possibly empty collection; must not return {@code null}
     */
    Collection<Descriptor> getDescriptors();

}
