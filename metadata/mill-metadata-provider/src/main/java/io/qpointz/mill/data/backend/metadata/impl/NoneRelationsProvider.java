package io.qpointz.mill.data.backend.metadata.impl;

import io.qpointz.mill.data.backend.metadata.RelationsProvider;
import io.qpointz.mill.data.backend.metadata.model.Relation;

import java.util.Collection;
import java.util.List;

public class NoneRelationsProvider implements RelationsProvider {
    @Override
    public Collection<Relation> getRelations() {
        return List.of();
    }
}
