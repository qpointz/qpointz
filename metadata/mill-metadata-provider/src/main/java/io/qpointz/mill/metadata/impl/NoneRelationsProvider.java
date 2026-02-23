package io.qpointz.mill.metadata.impl;

import io.qpointz.mill.metadata.RelationsProvider;
import io.qpointz.mill.metadata.model.Relation;

import java.util.Collection;
import java.util.List;

public class NoneRelationsProvider implements RelationsProvider {
    @Override
    public Collection<Relation> getRelations() {
        return List.of();
    }
}
