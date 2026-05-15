package io.qpointz.mill.resource;

/**
 * Optional capability implemented by Spring {@code org.springframework.core.io.Resource} types that
 * represent remote configuration objects. Supplies a credential-free stable key for metadata seed
 * ledgers and similar consumers.
 */
@FunctionalInterface
public interface MillConfigurationResourceKey {

    /**
     * Returns a deterministic ledger key that excludes credentials, SAS tokens, signed URL query
     * material, and other secret or volatile auth data.
     *
     * @return stable non-secret key string
     */
    String millConfigurationStableKey();
}
