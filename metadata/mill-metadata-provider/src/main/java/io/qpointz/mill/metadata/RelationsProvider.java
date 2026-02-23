package io.qpointz.mill.metadata;

import io.qpointz.mill.metadata.model.Relation;

import java.util.Collection;

public interface RelationsProvider {
    Collection<Relation> getRelations();
}
