package io.qpointz.mill.services.metadata.impl;

import io.qpointz.mill.services.metadata.RelationsProvider;
import io.qpointz.mill.services.metadata.model.Relation;

import java.util.Collection;
import java.util.List;

public class NoneRelationsProvider implements RelationsProvider {
    @Override
    public Collection<Relation> getRelations() {
        return List.of();
    }
}
