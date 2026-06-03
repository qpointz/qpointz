package io.qpointz.mill.security.authentication.basic;

import org.springframework.core.env.Environment;

/**
 * Resolves {@code mill.security.authentication.basic.store} and legacy {@code file-store}.
 */
public final class BasicAuthenticationStoreSupport {

    /** Value of {@code mill.security.authentication.basic.store} for JPA-backed credentials. */
    public static final String STORE_JPA = "jpa";

    public static final String PREFIX = "mill.security.authentication.basic.";
    public static final String STORE_PROPERTY = PREFIX + "store";
    public static final String LEGACY_FILE_STORE_PROPERTY = PREFIX + "file-store";
    public static final String ENABLE_PROPERTY = PREFIX + "enable";

    private BasicAuthenticationStoreSupport() {
    }

    /**
     * @param environment Spring environment
     * @return configured store value, or legacy {@code file-store} when {@code store} is unset
     */
    public static String effectiveStore(Environment environment) {
        String store = environment.getProperty(STORE_PROPERTY);
        if (store != null && !store.isBlank()) {
            return store.strip();
        }
        String legacy = environment.getProperty(LEGACY_FILE_STORE_PROPERTY);
        return legacy == null ? "" : legacy.strip();
    }

    /**
     * @param environment Spring environment
     * @return {@code true} when basic authentication is enabled
     */
    public static boolean isBasicEnabled(Environment environment) {
        return environment.getProperty(ENABLE_PROPERTY, Boolean.class, false);
    }

    /**
     * @param store effective store value from {@link #effectiveStore(Environment)}
     * @return {@code true} when {@code store} selects the JPA credential store
     */
    public static boolean isJpaStore(String store) {
        return STORE_JPA.equalsIgnoreCase(store == null ? "" : store.strip());
    }

    /**
     * @param store effective store value from {@link #effectiveStore(Environment)}
     * @return {@code true} when {@code store} is a non-JPA resource location (file, classpath, etc.)
     */
    public static boolean isFileResourceStore(String store) {
        return store != null && !store.isBlank() && !isJpaStore(store);
    }
}
