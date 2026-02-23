package io.qpointz.mill.metadata.repository;

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;

import java.util.Collection;
import java.util.Optional;

public interface FacetTypeRepository {
    void save(FacetTypeDescriptor descriptor);
    Optional<FacetTypeDescriptor> findByTypeKey(String typeKey);
    Collection<FacetTypeDescriptor> findAll();
    void deleteByTypeKey(String typeKey);
    boolean existsByTypeKey(String typeKey);
}
