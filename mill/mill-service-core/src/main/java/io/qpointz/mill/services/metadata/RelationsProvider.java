package io.qpointz.mill.services.metadata;

import io.qpointz.mill.services.metadata.model.Relation;

import java.util.Collection;

public interface RelationsProvider {
    Collection<Relation> getRelations();
}
