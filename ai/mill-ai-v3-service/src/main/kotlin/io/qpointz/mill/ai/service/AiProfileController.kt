package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.service.dto.AgentProfileResponse
import io.qpointz.mill.excepions.statuses.MillStatusDetails
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Read-only HTTP API for agent profiles registered in [ProfileRegistry].
 *
 * Thin transport: maps registry results to [AgentProfileResponse]; no persistence.
 */
@Tag(name = "ai-profiles", description = "Agent profile discovery for AI v3 chats")
@RestController
@RequestMapping(
    value = ["/api/v1/ai/profiles"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:8080"])
class AiProfileController(
    private val profileRegistry: ProfileRegistry,
) {

    @Operation(summary = "List registered agent profiles")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Profiles returned",
            content = [Content(array = ArraySchema(schema = Schema(implementation = AgentProfileResponse::class)))],
        ),
    ])
    @GetMapping(consumes = [MediaType.ALL_VALUE])
    fun listProfiles(): List<AgentProfileResponse> =
        profileRegistry.registeredProfiles().map(AgentProfileResponse::from)

    @Operation(summary = "Get a single agent profile by id")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Profile found",
            content = [Content(schema = Schema(implementation = AgentProfileResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Unknown profile id",
            content = [Content(schema = Schema(implementation = MillStatusDetails::class))],
        ),
    ])
    @GetMapping(value = ["/{profileId}"], consumes = [MediaType.ALL_VALUE])
    fun getProfile(@PathVariable profileId: String): AgentProfileResponse =
        profileRegistry.resolve(profileId)
            ?.let(AgentProfileResponse::from)
            ?: throw MillStatuses.notFound("Unknown profile: $profileId")
}
