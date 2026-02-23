package io.qpointz.mill.metadata.api

import io.qpointz.mill.metadata.api.dto.MetadataEntityDto
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataType
import io.qpointz.mill.metadata.service.MetadataService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.Optional

@WebMvcTest(controllers = [MetadataController::class])
@AutoConfigureMockMvc(addFilters = false)
class MetadataControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var metadataService: MetadataService

    @MockitoBean
    private lateinit var dtoMapper: DtoMapper

    @Test
    fun shouldGetEntityById() {
        val entity = MetadataEntity(
            id = "test.entity", type = MetadataType.TABLE, schemaName = "moneta", tableName = "clients",
            createdAt = Instant.now(), updatedAt = Instant.now()
        )
        whenever(metadataService.findById("test.entity")).thenReturn(Optional.of(entity))
        whenever(dtoMapper.toDto(any(), any())).thenAnswer {
            MetadataEntityDto(id = "test.entity", type = MetadataType.TABLE, schemaName = "moneta", tableName = "clients")
        }

        mockMvc.perform(get("/api/metadata/v1/entities/test.entity"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("test.entity"))
            .andExpect(jsonPath("$.type").value("TABLE"))
            .andExpect(jsonPath("$.tableName").value("clients"))
    }

    @Test
    fun shouldReturnNotFound_whenEntityNotExists() {
        whenever(metadataService.findById("nonexistent")).thenReturn(Optional.empty())
        mockMvc.perform(get("/api/metadata/v1/entities/nonexistent"))
            .andExpect(status().isNotFound)
    }
}
