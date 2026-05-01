package io.qpointz.mill.service.service;

import io.qpointz.mill.service.configuration.ApplicationDescriptor;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorSource;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Assembles the {@code /.well-known/mill} JSON from all {@link Descriptor} Spring beans plus
 * {@link DescriptorSource} batches (for example schema names from Calcite).
 *
 * <p>The {@code app} entry is derived from the first {@link Descriptor} whose {@link Descriptor#getTypeName()}
 * equals {@link DescriptorTypes#APP_TYPE_NAME} (typically {@link io.qpointz.mill.service.configuration.ApplicationDescriptor}).
 */
@Service
public class WellKnownService {

    private final List<Descriptor> descriptors;
    private final List<DescriptorSource> descriptorSources;

    /**
     * @param descriptors optional beans implementing {@link Descriptor} (may be {@code null} if absent)
     * @param sources     optional {@link DescriptorSource} contributors (may be {@code null} if absent)
     */
    public WellKnownService(@Autowired(required = false) List<Descriptor> descriptors,
                            @Autowired(required = false) List<DescriptorSource> sources) {
        this.descriptors = descriptors == null ? List.of() : descriptors;
        this.descriptorSources = sources == null ? List.of() : sources;
    }

    /**
     * Flattens all {@link Descriptor} beans and {@link DescriptorSource#getDescriptors()} contributions, then
     * groups by {@link Descriptor#getTypeName()}. The first descriptor with type {@link DescriptorTypes#APP_TYPE_NAME}
     * is also exposed under the {@code app} key for backward-compatible clients.
     *
     * @return map suitable for JSON serialization (keys include {@code app} and logical type names such as
     * {@link DescriptorTypes#SERVICE_TYPE_NAME}, {@link DescriptorTypes#SECURITY_TYPE_NAME}, etc.)
     */
    private Map<String, ?> allDescriptors() {
        val allDescriptors = new ArrayList<Descriptor>();
        allDescriptors.addAll(this.descriptors);
        this.descriptorSources.forEach(source -> allDescriptors.addAll(source.getDescriptors()));
        val descriptorsMap = allDescriptors.stream()
                .collect(Collectors.groupingBy(Descriptor::getTypeName));
        val map = new HashMap<String, Object>();
        map.putAll(descriptorsMap);

        if (descriptorsMap.containsKey(DescriptorTypes.APP_TYPE_NAME)) {
            val allApps = descriptorsMap.get(DescriptorTypes.APP_TYPE_NAME);
            map.put("app", allApps.get(0));
        }

        return map;
    }

    /**
     * Public API used by {@link io.qpointz.mill.service.controllers.ApplicationDescriptorController}.
     *
     * @return combined discovery map for JSON encoding
     */
    public Map<String, ?> metaInfo() {
        return allDescriptors();
    }
}
