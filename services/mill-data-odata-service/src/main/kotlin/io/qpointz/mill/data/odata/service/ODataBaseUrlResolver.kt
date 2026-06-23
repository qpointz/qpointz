package io.qpointz.mill.data.odata.service

import io.qpointz.mill.service.providers.ExternalHostsProvider
import jakarta.servlet.http.HttpServletRequest

/**
 * Resolves the public base URL for absolute OData discovery links.
 */
class ODataBaseUrlResolver(
    private val serviceProperties: ODataServiceProperties,
    private val externalHostsProvider: ExternalHostsProvider?,
) {

    /**
     * @param request current servlet request (fallback when externals are not configured)
     * @return origin without trailing slash (e.g. {@code https://host:8443})
     */
    fun origin(request: HttpServletRequest): String {
        val hostRef = serviceProperties.externalHost
        if (externalHostsProvider != null && hostRef.isNotBlank()) {
            val ext = externalHostsProvider.getExternals()[hostRef]
            if (ext != null) {
                return ext.asUrl()
            }
        }
        val scheme = request.scheme
        val host = request.serverName
        val port = request.serverPort
        val portPart = if ((scheme.equals("http", ignoreCase = true) && port != 80) ||
            (scheme.equals("https", ignoreCase = true) && port != 443)
        ) {
            ":$port"
        } else {
            ""
        }
        return "$scheme://$host$portPart"
    }
}
