package io.qpointz.mill.data.odata.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import io.qpointz.mill.service.descriptors.ServiceAddressScheme
import io.qpointz.mill.service.providers.ExternalHostLookup
import io.qpointz.mill.service.providers.ExternalHostsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * OData HTTP connection hints for well-known discovery.
 */
@Component
@ConditionalOnService(value = "odata", group = "data")
class ODataConnectionDescriptor(
    serviceProperties: ODataServiceProperties,
    @Autowired(required = false) externalHosts: ExternalHostsProvider?,
) : Descriptor {

    private val externalHosts: ExternalHostsProvider? = externalHosts
    private val hostRef: String = serviceProperties.getExternalHost()

    /** Per-schema OData service root template; list schemas at {@code /services/odata/schemas}. */
    @JsonProperty("api-path")
    val path: String = "/services/odata/{schema}.svc/"

    /**
     * @return catalog path listing available schema service roots
     */
    @JsonProperty("catalog-path")
    fun getCatalogPath(): String = "/services/odata/schemas"

    fun getScheme(): String {
        val external = ExternalHostLookup.resolve(externalHosts, hostRef)
        return external?.scheme()?.toString()?.lowercase() ?: ServiceAddressScheme.HTTP.toString().lowercase()
    }

    fun getHost(): String = ExternalHostLookup.resolve(externalHosts, hostRef)?.host() ?: "localhost"

    fun getPort(): Int = ExternalHostLookup.resolve(externalHosts, hostRef)?.port() ?: 8080

    override fun getTypeName(): String = DescriptorTypes.CONNECTIONS_TYPE_NAME
}
