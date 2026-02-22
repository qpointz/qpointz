package io.qpointz.mill.data.backend.metadata;

import io.qpointz.mill.data.backend.metadata.model.Relation;

import java.util.Collection;

public interface RelationsProvider {
    Collection<Relation> getRelations();
}
