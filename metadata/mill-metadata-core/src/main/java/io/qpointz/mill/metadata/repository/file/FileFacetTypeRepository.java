package io.qpointz.mill.metadata.repository.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;
import io.qpointz.mill.metadata.repository.FacetTypeRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FileFacetTypeRepository implements FacetTypeRepository {

    private final Map<String, FacetTypeDescriptor> store = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper;
    private final ResourceResolver resourceResolver;
    private final List<String> locations;

    public FileFacetTypeRepository(List<String> locations, ResourceResolver resourceResolver) {
        this.locations = new ArrayList<>(locations);
        this.resourceResolver = resourceResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        load();
    }

    private void load() {
        for (String location : locations) {
            try {
                List<ResourceResolver.ResolvedResource> resources = resourceResolver.resolve(location);
                for (ResourceResolver.ResolvedResource resource : resources) {
                    loadFromResource(resource);
                }
            } catch (IOException e) {
                log.warn("Failed to resolve facet type location: {}", location, e);
            }
        }
        log.info("Loaded {} facet type descriptors from {} location(s)", store.size(), locations.size());
    }

    private void loadFromResource(ResourceResolver.ResolvedResource resource) {
        try (InputStream is = resource.inputStream()) {
            FacetTypeFileFormat format = yamlMapper.readValue(is, FacetTypeFileFormat.class);
            if (format.getFacetTypes() != null) {
                for (FacetTypeDescriptor descriptor : format.getFacetTypes()) {
                    store.put(descriptor.getTypeKey(), descriptor);
                    log.debug("Loaded facet type: {} from {}", descriptor.getTypeKey(), resource.name());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load facet types from: {}", resource.name(), e);
        }
    }

    @Override
    public void save(FacetTypeDescriptor descriptor) {
        store.put(descriptor.getTypeKey(), descriptor);
    }

    @Override
    public Optional<FacetTypeDescriptor> findByTypeKey(String typeKey) {
        return Optional.ofNullable(store.get(typeKey));
    }

    @Override
    public Collection<FacetTypeDescriptor> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    @Override
    public void deleteByTypeKey(String typeKey) {
        store.remove(typeKey);
    }

    @Override
    public boolean existsByTypeKey(String typeKey) {
        return store.containsKey(typeKey);
    }

    public static class FacetTypeFileFormat {
        @JsonProperty("facet-types")
        private List<FacetTypeDescriptor> facetTypes;

        public List<FacetTypeDescriptor> getFacetTypes() {
            return facetTypes != null ? facetTypes : List.of();
        }

        public void setFacetTypes(List<FacetTypeDescriptor> facetTypes) {
            this.facetTypes = facetTypes;
        }
    }
}
