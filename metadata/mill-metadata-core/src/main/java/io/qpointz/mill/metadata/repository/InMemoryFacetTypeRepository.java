package io.qpointz.mill.metadata.repository;

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFacetTypeRepository implements FacetTypeRepository {

    private final Map<String, FacetTypeDescriptor> store = new ConcurrentHashMap<>();

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
}
