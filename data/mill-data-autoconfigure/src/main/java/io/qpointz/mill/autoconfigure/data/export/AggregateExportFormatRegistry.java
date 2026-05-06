package io.qpointz.mill.autoconfigure.data.export;

import io.qpointz.mill.source.export.ExportFormatProvider;
import io.qpointz.mill.source.export.ExportFormatRegistry;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Aggregates {@link ExportFormatProvider} entries from the classpath via {@link ServiceLoader}.
 */
public class AggregateExportFormatRegistry implements ExportFormatRegistry {

    @Getter
    private final List<ExportFormatProvider> allProviders;

    private final Map<String, ExportFormatProvider> byId;

    /**
     * Loads providers using the given classloader (application classloader when {@code null}).
     *
     * @param classLoader loader for SPI discovery
     */
    public AggregateExportFormatRegistry(ClassLoader classLoader) {
        ClassLoader loader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        List<ExportFormatProvider> loaded = ServiceLoader.load(ExportFormatProvider.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        Map<String, ExportFormatProvider> map = new HashMap<>();
        for (ExportFormatProvider p : loaded) {
            String id = p.metadata().getId().trim().toLowerCase(Locale.ROOT);
            if (map.containsKey(id)) {
                throw new IllegalStateException("Duplicate export format id: " + id);
            }
            map.put(id, p);
        }
        this.byId = Collections.unmodifiableMap(map);
        this.allProviders = List.copyOf(loaded);
    }

    @Override
    public ExportFormatProvider get(String id) {
        if (id == null) {
            return null;
        }
        return byId.get(id.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public List<ExportFormatProvider> allProviders() {
        return allProviders;
    }
}
