package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.data.backend.export.ExportVectorBlockSource;
import io.qpointz.mill.source.export.ExportFormatProvider;
import io.qpointz.mill.source.export.ExportFormatRegistry;
import io.qpointz.mill.vectors.VectorBlockIterator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Orchestrates vector execution and SPI format encoding for HTTP export.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnService(value = "export", group = "data")
public class ExportFacility {

    private final ExportVectorBlockSource vectorBlockSource;
    private final ExportFormatRegistry formatRegistry;
    private final EffectiveExportFormats effectiveExportFormats;

    /**
     * Runs a plan-native full table scan and writes encoded bytes to {@code out}.
     *
     * @param schema   physical schema name
     * @param table    physical table name
     * @param formatId export format id
     * @param out      response stream
     */
    public void exportTable(String schema, String table, String formatId, OutputStream out) throws IOException {
        validate(formatId);
        VectorBlockIterator it = vectorBlockSource.exportTable(schema, table);
        write(it, formatId, out);
    }

    /**
     * Runs ad-hoc SQL and writes encoded bytes to {@code out}.
     *
     * @param sql      SQL body
     * @param formatId export format id
     * @param out      response stream
     */
    public void exportSql(String sql, String formatId, OutputStream out) throws IOException {
        validate(formatId);
        VectorBlockIterator it = vectorBlockSource.exportSql(sql);
        write(it, formatId, out);
    }

    private void validate(String formatId) {
        List<String> registered = formatRegistry.allProviders().stream()
                .map(p -> p.metadata().getId().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<String> allowed = effectiveExportFormats.effectiveFormatIds(registered);
        if (!effectiveExportFormats.isAllowed(formatId, allowed)) {
            throw new IllegalArgumentException("Unknown or disallowed export format: " + formatId);
        }
        if (formatRegistry.get(formatId) == null) {
            throw new IllegalArgumentException("Unknown export format: " + formatId);
        }
    }

    private void write(VectorBlockIterator iterator, String formatId, OutputStream out) throws IOException {
        ExportFormatProvider provider = formatRegistry.get(formatId);
        provider.encoder().encode(iterator, out);
    }

    /**
     * @return SPI providers visible on HTTP after allowlist filtering
     */
    public List<ExportFormatProvider> providersForHttp() {
        List<String> registered = formatRegistry.allProviders().stream()
                .map(p -> p.metadata().getId().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<String> allowed = effectiveExportFormats.effectiveFormatIds(registered);
        return formatRegistry.allProviders().stream()
                .filter(p -> allowed.contains(p.metadata().getId().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}
