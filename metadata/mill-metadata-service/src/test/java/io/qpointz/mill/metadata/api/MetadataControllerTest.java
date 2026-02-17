package io.qpointz.mill.metadata.api;

import io.qpointz.mill.metadata.api.dto.MetadataEntityDto;
import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.MetadataType;
import io.qpointz.mill.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
class MetadataControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private MetadataService metadataService;
    
    @MockitoBean
    private DtoMapper dtoMapper;
    
    @Test
    void shouldGetEntityById() throws Exception {
        MetadataEntity entity = new MetadataEntity();
        entity.setId("test.entity");
        entity.setType(MetadataType.TABLE);
        entity.setSchemaName("moneta");
        entity.setTableName("clients");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        
        when(metadataService.findById("test.entity")).thenReturn(Optional.of(entity));
        when(dtoMapper.toDto(eq(entity), any())).thenReturn(
            MetadataEntityDto.builder()
                .id("test.entity")
                .type(MetadataType.TABLE)
                .schemaName("moneta")
                .tableName("clients")
                .build()
        );
        
        mockMvc.perform(get("/api/metadata/v1/entities/test.entity"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("test.entity"))
            .andExpect(jsonPath("$.type").value("TABLE"))
            .andExpect(jsonPath("$.tableName").value("clients"));
    }
    
    @Test
    void shouldReturnNotFound_whenEntityNotExists() throws Exception {
        when(metadataService.findById("nonexistent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/metadata/v1/entities/nonexistent"))
            .andExpect(status().isNotFound());
    }
}

