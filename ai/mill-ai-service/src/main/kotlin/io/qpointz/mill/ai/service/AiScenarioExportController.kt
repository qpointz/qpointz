package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.scenario.ConversationScenarioExporter
import io.qpointz.mill.ai.scenario.ScenarioPackYamlWriter
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Dev/tuning endpoint for exporting live chats to draft scenario pack YAML.
 *
 * Registered only when `mill.ai.chat.scenario-capture.enabled=true`.
 */
@Tag(name = "ai-chat", description = "Unified AI v3 chat lifecycle and messaging")
@RestController
@RequestMapping("/api/v1/ai/chats")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
@ConditionalOnAiEnabled
@ConditionalOnProperty(
    prefix = "mill.ai.chat.scenario-capture",
    name = ["enabled"],
    havingValue = "true",
)
class AiScenarioExportController(
    private val chatService: ChatService,
    private val scenarioExporter: ConversationScenarioExporter,
    private val yamlWriter: ScenarioPackYamlWriter = ScenarioPackYamlWriter(),
) {

    /**
     * Exports a chat conversation as a draft [ScenarioPack] YAML document.
     *
     * @param chatId Conversation id.
     * @param format `yaml` (default) or `json` for the pack body without comment header.
     */
    @Operation(
        summary = "Export chat to draft scenario pack YAML",
        description = "Dev/tuning only. Requires mill.ai.chat.scenario-capture.enabled=true. " +
            "Returns ask steps and best-effort script; add verify: manually.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Scenario pack exported"),
            ApiResponse(responseCode = "404", description = "Chat not found"),
            ApiResponse(responseCode = "422", description = "Chat has no exportable turns"),
        ],
    )
    @GetMapping(
        value = ["/{chatId}/scenario-export"],
        produces = [MediaType.APPLICATION_JSON_VALUE, "application/x-yaml", MediaType.TEXT_PLAIN_VALUE],
        consumes = [MediaType.ALL_VALUE],
    )
    fun exportScenario(
        @PathVariable chatId: String,
        @RequestParam(defaultValue = "yaml") format: String,
    ): ResponseEntity<String> {
        chatService.getChat(chatId) ?: throw MillStatuses.notFound("Chat not found: $chatId")

        val export = try {
            scenarioExporter.export(chatId)
        } catch (ex: IllegalStateException) {
            throw MillStatuses.unprocessable(ex.message ?: "Chat not exportable")
        } ?: throw MillStatuses.notFound("Chat not found: $chatId")

        val filename = "${export.pack.slug()}.${if (format == "json") "json" else "yml"}"
        val body = when (format.lowercase()) {
            "json" -> ScenarioPackYamlWriter.jsonMapper.writeValueAsString(export.pack)
            else -> yamlWriter.write(export)
        }
        val mediaType = when (format.lowercase()) {
            "json" -> MediaType.APPLICATION_JSON
            else -> MediaType.parseMediaType("application/x-yaml")
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(filename).build().toString(),
            )
            .body(body)
    }
}
