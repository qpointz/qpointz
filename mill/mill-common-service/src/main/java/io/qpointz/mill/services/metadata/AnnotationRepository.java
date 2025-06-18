package io.qpointz.mill.services.metadata;

import java.util.Collection;
import java.util.Optional;

public interface AnnotationRepository {
    Collection<AnnotationEntry> listAnnotation(String targetType, String targetId);
    Optional<AnnotationEntry> getAnnotation(String targetType, String targetId, String annotationType);
    void annotate(AnnotationEntry entry);
    void removeAnnotation(String targetType, String targetId, String annotationType);
}
