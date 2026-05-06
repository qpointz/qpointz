package io.qpointz.mill.export;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for format allowlist resolution (HTTP-effective ids).
 */
class EffectiveExportFormatsTest {

    @Test
    void emptyFormatsProperty_exposesAllRegistered() {
        ExportServiceProperties props = new ExportServiceProperties();
        props.setFormats(List.of());
        EffectiveExportFormats cut = new EffectiveExportFormats(props);
        List<String> reg = List.of("csv", "json", "avro");
        assertThat(cut.effectiveFormatIds(reg)).containsExactlyElementsOf(reg);
    }

    @Test
    void starWildcard_exposesAllRegistered() {
        ExportServiceProperties props = new ExportServiceProperties();
        props.setFormats(List.of("  *  "));
        EffectiveExportFormats cut = new EffectiveExportFormats(props);
        List<String> reg = List.of("csv", "json");
        assertThat(cut.effectiveFormatIds(reg)).containsExactlyElementsOf(reg);
    }

    @Test
    void explicitSubset_filtersAndPreservesOrder() {
        ExportServiceProperties props = new ExportServiceProperties();
        props.setFormats(List.of("json", "csv"));
        EffectiveExportFormats cut = new EffectiveExportFormats(props);
        assertThat(cut.effectiveFormatIds(List.of("csv", "json", "tsv"))).containsExactly("json", "csv");
    }

    @Test
    void isAllowed_respectsEffectiveList() {
        ExportServiceProperties props = new ExportServiceProperties();
        props.setFormats(List.of("csv"));
        EffectiveExportFormats cut = new EffectiveExportFormats(props);
        List<String> eff = cut.effectiveFormatIds(List.of("csv", "json"));
        assertThat(cut.isAllowed("csv", eff)).isTrue();
        assertThat(cut.isAllowed("json", eff)).isFalse();
    }
}
