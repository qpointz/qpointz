package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Resolves {@code mill.data.services.export.formats} allowlist against SPI-registered providers.
 */
@Slf4j
@Component
@ConditionalOnService(value = "export", group = "data")
public class EffectiveExportFormats {

    private final ExportServiceProperties properties;

    /**
     * @param properties export service configuration
     */
    public EffectiveExportFormats(ExportServiceProperties properties) {
        this.properties = properties;
    }

    /**
     * @param registeredIds lowercase ids currently registered in {@link io.qpointz.mill.source.export.ExportFormatRegistry}
     * @return ids accepted on HTTP when {@link ExportServiceProperties#getFormats()} is applied
     */
    public List<String> effectiveFormatIds(List<String> registeredIds) {
        List<String> configured = properties.getFormats();
        if (configured == null || configured.isEmpty()) {
            return List.copyOf(registeredIds);
        }
        for (String token : configured) {
            if (token != null && "*".equals(token.trim())) {
                return List.copyOf(registeredIds);
            }
        }
        Set<String> reg = new LinkedHashSet<>(registeredIds);
        List<String> out = new ArrayList<>();
        for (String raw : configured) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String id = raw.trim().toLowerCase(Locale.ROOT);
            if (!reg.contains(id)) {
                log.warn("mill.data.services.export.formats: unknown format id '{}', ignoring", raw);
                continue;
            }
            out.add(id);
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * @param formatId requested format query parameter
     * @param allowed    effective allowed ids
     * @return true if {@code formatId} is permitted
     */
    public boolean isAllowed(String formatId, List<String> allowed) {
        if (formatId == null || formatId.isBlank()) {
            return false;
        }
        String id = formatId.trim().toLowerCase(Locale.ROOT);
        return allowed.contains(id);
    }
}
