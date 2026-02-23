package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataTargetType;
import io.qpointz.mill.metadata.domain.ValidationResult;

import java.util.Collection;
import java.util.Optional;

public interface FacetCatalog {

    void register(FacetTypeDescriptor descriptor);
    void update(FacetTypeDescriptor descriptor);
    void delete(String typeKey);

    Optional<FacetTypeDescriptor> get(String typeKey);
    Collection<FacetTypeDescriptor> getAll();
    Collection<FacetTypeDescriptor> getEnabled();
    Collection<FacetTypeDescriptor> getMandatory();
    Collection<FacetTypeDescriptor> getForTargetType(MetadataTargetType targetType);

    boolean isAllowed(String typeKey);
    boolean isMandatory(String typeKey);
    boolean isApplicableTo(String typeKey, MetadataTargetType targetType);

    ValidationResult validateFacetContent(String typeKey, Object facetData);
    ValidationResult validateEntityFacets(MetadataEntity entity);
}
