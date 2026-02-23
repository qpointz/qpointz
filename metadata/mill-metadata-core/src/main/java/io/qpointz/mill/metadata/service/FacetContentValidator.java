package io.qpointz.mill.metadata.service;

import io.qpointz.mill.metadata.domain.ValidationResult;

import java.util.Map;

public interface FacetContentValidator {
    ValidationResult validate(Map<String, Object> contentSchema, Object facetData);
}
