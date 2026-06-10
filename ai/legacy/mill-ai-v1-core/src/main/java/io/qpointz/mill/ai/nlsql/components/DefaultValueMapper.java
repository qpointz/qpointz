package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback {@link ValueMapper} that simply echoes the resolved value produced by the NL2SQL model.
 * Useful when no external value repository is configured.
 */
@Slf4j
public class DefaultValueMapper implements ValueMapper  {
    @Override
    public MappedValue mapValue(PlaceholderMapping mapping) {
        log.info("Mapping value of '{}' to value:'{}' display:{} placeholder:{} ", mapping.target(), mapping.resolvedValue(), mapping.display(), mapping.placeholder());
        return new MappedValue(mapping, mapping.resolvedValue());
    }
}
