package io.qpointz.mill.data.backend.metadata.impl.file;

import io.qpointz.mill.data.backend.metadata.RelationsProvider;
import io.qpointz.mill.data.backend.metadata.model.Relation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FileRelationsProvider implements RelationsProvider {

    private final FileRepository repository;

    public FileRelationsProvider(FileRepository repository) {
        log.info("Using file-based relations repository");
        this.repository = repository;
    }

    @Override
    public Collection<Relation> getRelations() {
        return this.repository.schemas().stream().map(schema -> schema.relations().stream()
                .map(rel -> new Relation(
                         new Relation.TableRef(schema.name(), rel.parent().table()),
                         new Relation.TableRef(schema.name(), rel.child().table()),
                         new Relation.AttributeRelation(
                                 new Relation.AttributeRef(rel.parent().attribute()),
                                 new Relation.AttributeRef(rel.child().attribute())
                         ),
                         asCardinality(rel.cardinality()),
                         rel.description())).toList())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Relation.Cardinality asCardinality(Optional<String> cardinality) {
        if (cardinality == null || cardinality.isEmpty()) {
            return Relation.Cardinality.UNSPECIFIED;
        }

        val cardString = cardinality.get()
                .trim()
                .toUpperCase();

        if (cardString.isEmpty()) {
            return Relation.Cardinality.UNSPECIFIED;
        }

        return switch (cardString) {
            case "1-1" -> Relation.Cardinality.ONE_TO_ONE;
            case "1-*", "1-N" -> Relation.Cardinality.ONE_TO_MANY;
            case "*-*", "N-N" -> Relation.Cardinality.MANY_TO_MANY;
            default -> Relation.Cardinality.UNSPECIFIED;
        };
    }
}
