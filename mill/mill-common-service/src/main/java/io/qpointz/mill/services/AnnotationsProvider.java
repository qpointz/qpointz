package io.qpointz.mill.services;

import io.qpointz.mill.services.metadata.ColumnAnnotations;
import io.qpointz.mill.services.metadata.InstanceAnnotations;
import io.qpointz.mill.services.metadata.SchemaAnnotations;
import io.qpointz.mill.services.metadata.TableAnnotations;

public interface AnnotationsProvider {
    InstanceAnnotations getInstanceAnnotations();
    SchemaAnnotations getSchemaAnnotations();
    TableAnnotations getTableAnnotations();
    ColumnAnnotations getColumnAnnotations();
}
