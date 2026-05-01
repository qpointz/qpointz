package io.qpointz.mill.ui;

import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Well-known entry for the embedded Mill UI when {@code mill.ui.enabled} is true (default).
 *
 * <p>Advertises the SPA shell under the {@link DescriptorTypes#SERVICE_TYPE_NAME} bucket alongside
 * data-plane descriptors.
 */
@Data
@Component
@ConditionalOnProperty(name = "mill.ui.enabled", havingValue = "true", matchIfMissing = true)
public class MillUiServiceDescriptor implements Descriptor {

    /** Logical id for this surface in discovery JSON. */
    @Getter
    private final String name = "ui";

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.SERVICE_TYPE_NAME;
    }

}
