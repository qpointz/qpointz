package io.qpointz.mill.export;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.source.export.ExportFormatMetadata;
import io.qpointz.mill.source.export.ExportFormatProvider;
import io.qpointz.mill.source.export.ExportFormatRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP tests for {@link ExportRestController} without a Spring Boot application context
 * (this module is auto-configuration only; {@link io.qpointz.mill.annotations.service.ConditionalOnService}
 * would otherwise omit the controller from slice tests).
 */
@ExtendWith(MockitoExtension.class)
class ExportRestControllerTest {

    @Mock
    private ExportFacility exportFacility;

    @Mock
    private SchemaProvider schemaProvider;

    @Mock
    private ExportBaseUrlResolver exportBaseUrlResolver;

    @Mock
    private EffectiveExportFormats effectiveExportFormats;

    @Mock
    private ExportFormatRegistry exportFormatRegistry;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ExportRestController(
                                exportFacility,
                                schemaProvider,
                                exportBaseUrlResolver,
                                effectiveExportFormats,
                                exportFormatRegistry))
                .build();
    }

    @Test
    void formats_returnsProviderMetadata() throws Exception {
        ExportFormatProvider csv = provider("csv", "text/csv", "csv");
        when(exportFacility.providersForHttp()).thenReturn(List.of(csv));

        mockMvc.perform(get("/services/export/formats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("csv"))
                .andExpect(jsonPath("$[0].mediaType").value("text/csv"))
                .andExpect(jsonPath("$[0].fileExtension").value("csv"));
    }

    private static ExportFormatProvider provider(String id, String mediaType, String ext) {
        return new ExportFormatProvider() {
            @Override
            public ExportFormatMetadata metadata() {
                return new ExportFormatMetadata(id, mediaType, ext);
            }

            @Override
            public io.qpointz.mill.source.export.StreamingExportEncoder encoder() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
