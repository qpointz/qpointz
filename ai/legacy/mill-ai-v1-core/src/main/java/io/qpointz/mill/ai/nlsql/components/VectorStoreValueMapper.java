package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.ai.nlsql.ValueRepository;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * {@link ValueMapper} implementation that delegates to a {@link ValueRepository} for resolving
 * placeholders. If no match is found in the repository the original value is returned.
 */
@AllArgsConstructor
public class VectorStoreValueMapper implements ValueMapper {

    private final ValueRepository valueRepository;

    @Override
    public MappedValue mapValue(PlaceholderMapping mapping) {
        val lookup = this.valueRepository.lookupValue(mapping.targetAsId(), mapping.resolvedValue());
        return  new MappedValue(mapping, lookup.orElse(mapping.resolvedValue()));
    }
}
