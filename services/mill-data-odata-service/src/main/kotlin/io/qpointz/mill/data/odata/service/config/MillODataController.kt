package io.qpointz.mill.data.odata.service.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * OData v4 controller mounted under the Mill data-plane base path.
 */
@RestController
@RequestMapping("/services/odata/{schema}.svc")
@ConditionalOnService(value = "odata", group = "data")
class MillODataController(
    private val syncService: MillODataSyncService,
) {

    /**
     * Delegates OData protocol handling to the synchronous RWS pipeline.
     *
     * @param schema physical schema name from the URL path
     * @param request inbound servlet request
     * @param response outbound servlet response
     */
    @RequestMapping(
        value = ["/**"],
        method = [
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.PATCH,
            RequestMethod.DELETE,
        ],
    )
    @Throws(Exception::class)
    fun serve(
        @PathVariable schema: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        syncService.handle(schema, request, response)
    }
}
