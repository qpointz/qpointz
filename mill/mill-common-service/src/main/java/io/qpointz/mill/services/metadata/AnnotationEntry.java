package io.qpointz.mill.services.metadata;

public interface AnnotationEntry {
    String annotationType();
    String targetType();
    String target();
    String value();
}
