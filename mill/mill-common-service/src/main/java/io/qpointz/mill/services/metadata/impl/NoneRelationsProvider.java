package io.qpointz.mill.services.metadata.impl;

import io.qpointz.mill.services.metadata.RelationsProvider;
import io.qpointz.mill.services.metadata.model.Relation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Lazy
@Component
@ConditionalOnProperty(prefix = "mill.metadata", name = "relations", havingValue = "none")
public class NoneRelationsProvider implements RelationsProvider {
    @Override
    public Collection<Relation> getRelations() {
        return List.of();
    }
}
