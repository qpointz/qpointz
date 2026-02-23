package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.*;
import io.qpointz.mill.metadata.repository.FacetTypeRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DefaultFacetCatalog implements FacetCatalog {

    private final FacetTypeRepository repository;
    private final FacetContentValidator contentValidator;

    public DefaultFacetCatalog(FacetTypeRepository repository) {
        this(repository, null);
    }

    public DefaultFacetCatalog(FacetTypeRepository repository, FacetContentValidator contentValidator) {
        this.repository = repository;
        this.contentValidator = contentValidator;
    }

    @Override
    public void register(FacetTypeDescriptor descriptor) {
        if (repository.existsByTypeKey(descriptor.getTypeKey())) {
            throw new IllegalArgumentException("Facet type already registered: " + descriptor.getTypeKey());
        }
        repository.save(descriptor);
        log.info("Registered facet type: {}", descriptor.getTypeKey());
    }

    @Override
    public void update(FacetTypeDescriptor descriptor) {
        Optional<FacetTypeDescriptor> existing = repository.findByTypeKey(descriptor.getTypeKey());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Facet type not found: " + descriptor.getTypeKey());
        }
        if (existing.get().isMandatory() && !descriptor.isEnabled()) {
            throw new IllegalArgumentException("Cannot disable mandatory facet type: " + descriptor.getTypeKey());
        }
        repository.save(descriptor);
        log.info("Updated facet type: {}", descriptor.getTypeKey());
    }

    @Override
    public void delete(String typeKey) {
        Optional<FacetTypeDescriptor> existing = repository.findByTypeKey(typeKey);
        if (existing.isEmpty()) {
            return;
        }
        if (existing.get().isMandatory()) {
            throw new IllegalArgumentException("Cannot delete mandatory facet type: " + typeKey);
        }
        repository.deleteByTypeKey(typeKey);
        log.info("Deleted facet type: {}", typeKey);
    }

    @Override
    public Optional<FacetTypeDescriptor> get(String typeKey) {
        return repository.findByTypeKey(typeKey);
    }

    @Override
    public Collection<FacetTypeDescriptor> getAll() {
        return repository.findAll();
    }

    @Override
    public Collection<FacetTypeDescriptor> getEnabled() {
        return repository.findAll().stream()
                .filter(FacetTypeDescriptor::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FacetTypeDescriptor> getMandatory() {
        return repository.findAll().stream()
                .filter(FacetTypeDescriptor::isMandatory)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FacetTypeDescriptor> getForTargetType(MetadataTargetType targetType) {
        return repository.findAll().stream()
                .filter(d -> d.isApplicableTo(targetType))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAllowed(String typeKey) {
        return repository.findByTypeKey(typeKey)
                .map(FacetTypeDescriptor::isEnabled)
                .orElse(true);
    }

    @Override
    public boolean isMandatory(String typeKey) {
        return repository.findByTypeKey(typeKey)
                .map(FacetTypeDescriptor::isMandatory)
                .orElse(false);
    }

    @Override
    public boolean isApplicableTo(String typeKey, MetadataTargetType targetType) {
        return repository.findByTypeKey(typeKey)
                .map(d -> d.isApplicableTo(targetType))
                .orElse(true);
    }

    @Override
    public ValidationResult validateFacetContent(String typeKey, Object facetData) {
        Optional<FacetTypeDescriptor> descriptorOpt = repository.findByTypeKey(typeKey);
        if (descriptorOpt.isEmpty()) {
            return ValidationResult.ok();
        }
        FacetTypeDescriptor descriptor = descriptorOpt.get();
        if (!descriptor.hasContentSchema()) {
            return ValidationResult.ok();
        }
        if (contentValidator == null) {
            log.debug("No content validator configured, skipping schema validation for: {}", typeKey);
            return ValidationResult.ok();
        }
        return contentValidator.validate(descriptor.getContentSchema(), facetData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validateEntityFacets(MetadataEntity entity) {
        List<ValidationResult> results = new ArrayList<>();

        MetadataTargetType entityTargetType = toTargetType(entity.getType());

        Map<String, Map<String, Object>> facets = entity.getFacets();
        if (facets == null || facets.isEmpty()) {
            return ValidationResult.ok();
        }

        for (Map.Entry<String, Map<String, Object>> facetEntry : facets.entrySet()) {
            String typeKey = facetEntry.getKey();

            Optional<FacetTypeDescriptor> descriptorOpt = repository.findByTypeKey(typeKey);
            if (descriptorOpt.isEmpty()) {
                continue;
            }

            FacetTypeDescriptor descriptor = descriptorOpt.get();

            if (!descriptor.isEnabled()) {
                results.add(ValidationResult.fail(
                        "Facet type '" + typeKey + "' is disabled"));
                continue;
            }

            if (!descriptor.isApplicableTo(entityTargetType)) {
                results.add(ValidationResult.fail(
                        "Facet type '" + typeKey + "' is not applicable to " + entityTargetType));
                continue;
            }

            if (descriptor.hasContentSchema() && contentValidator != null) {
                Map<String, Object> scopedFacets = facetEntry.getValue();
                if (scopedFacets != null) {
                    for (Map.Entry<String, Object> scopeEntry : scopedFacets.entrySet()) {
                        ValidationResult scopeResult = contentValidator.validate(
                                descriptor.getContentSchema(), scopeEntry.getValue());
                        if (!scopeResult.valid()) {
                            results.add(ValidationResult.fail(
                                    scopeResult.errors().stream()
                                            .map(e -> typeKey + "[" + scopeEntry.getKey() + "]: " + e)
                                            .toList()));
                        }
                    }
                }
            }
        }

        return results.isEmpty() ? ValidationResult.ok() : ValidationResult.merge(results);
    }

    private MetadataTargetType toTargetType(MetadataType type) {
        if (type == null) {
            return MetadataTargetType.ANY;
        }
        return switch (type) {
            case CATALOG -> MetadataTargetType.CATALOG;
            case SCHEMA -> MetadataTargetType.SCHEMA;
            case TABLE -> MetadataTargetType.TABLE;
            case ATTRIBUTE -> MetadataTargetType.ATTRIBUTE;
            case CONCEPT -> MetadataTargetType.CONCEPT;
        };
    }
}
