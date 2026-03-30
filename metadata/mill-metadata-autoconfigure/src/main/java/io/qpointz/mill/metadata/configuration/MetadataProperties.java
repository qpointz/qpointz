package io.qpointz.mill.metadata.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration binding for metadata repository backend and facet registry behaviour.
 *
 * <p>Bound to the {@code mill.metadata} prefix. Use {@link Repository} to select the active
 * backend ({@code file} by default) and {@link Repository.File} for file-backed paths and
 * optional watch / write flags (SPEC §14.0, §15.1).
 */
@ConfigurationProperties(prefix = "mill.metadata")
public class MetadataProperties {

    /** Repository backend selector and nested file settings; defaults to {@code new Repository()}. */
    private Repository repository = new Repository();

    /** Facet type registry source strategy; defaults to {@code new FacetTypeRegistry()}. */
    private FacetTypeRegistry facetTypeRegistry = new FacetTypeRegistry();

    /**
     * Returns the repository configuration.
     *
     * @return current {@link Repository} settings
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Sets the repository configuration.
     *
     * @param repository replacement {@link Repository} instance
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Returns facet type registry strategy configuration.
     *
     * @return current {@link FacetTypeRegistry} settings
     */
    public FacetTypeRegistry getFacetTypeRegistry() {
        return facetTypeRegistry;
    }

    /**
     * Sets facet type registry strategy configuration.
     *
     * @param facetTypeRegistry replacement {@link FacetTypeRegistry} instance
     */
    public void setFacetTypeRegistry(FacetTypeRegistry facetTypeRegistry) {
        this.facetTypeRegistry = facetTypeRegistry;
    }

    /**
     * Metadata repository implementation selector ({@code mill.metadata.repository}).
     */
    public static class Repository {

        /**
         * Backend type identifier.
         *
         * <p>Supported values:
         * <ul>
         *   <li>{@code file} — YAML-backed in-process repository loaded from configured resources (default)</li>
         *   <li>{@code jpa}  — relational JPA repository (requires {@code mill-metadata-persistence})</li>
         *   <li>{@code noop} — no-op repositories only</li>
         * </ul>
         */
        private String type = "file";

        /** File-backend settings when {@code type=file}; defaults to {@code new File()}. */
        private File file = new File();

        /**
         * Returns the repository type identifier.
         *
         * @return backend type string
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the repository type identifier.
         *
         * @param type backend type string
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Returns nested file-backend configuration.
         *
         * @return file settings
         */
        public File getFile() {
            return file;
        }

        /**
         * Sets nested file-backend configuration.
         *
         * @param file replacement {@link File} instance
         */
        public void setFile(File file) {
            this.file = file;
        }

        /**
         * File-repository specific configuration ({@code mill.metadata.repository.file}).
         */
        public static class File {

            /**
             * Comma-separated list of Spring resource paths for YAML metadata files.
             *
             * <p>Supports classpath ({@code classpath:}) and filesystem ({@code file:}) prefixes,
             * as well as Ant-style glob patterns.
             * When {@code mill.metadata.repository.type=file}, this must be set (non-blank) or startup fails fast.
             */
            private String path;

            /**
             * Whether writes may be persisted back to files (SPEC §15.1).
             *
             * <p>Reserved for a future file adapter; currently has no effect.
             */
            private boolean writable = false;

            /**
             * Whether to watch backing files for changes and reload automatically.
             *
             * <p>Not yet implemented; reserved for future use. Defaults to {@code false}.
             */
            private boolean watch = false;

            /**
             * Returns the comma-separated list of metadata file resource paths.
             *
             * @return resource path string
             */
            public String getPath() {
                return path;
            }

            /**
             * Sets the comma-separated list of metadata file resource paths.
             *
             * @param path resource path string
             */
            public void setPath(String path) {
                this.path = path;
            }

            /**
             * Returns whether write-back to files is enabled.
             *
             * @return {@code true} if writable mode is requested
             */
            public boolean isWritable() {
                return writable;
            }

            /**
             * Sets whether write-back to files is enabled.
             *
             * @param writable {@code true} to request writable file mode
             */
            public void setWritable(boolean writable) {
                this.writable = writable;
            }

            /**
             * Returns whether file-watch hot-reload is enabled.
             *
             * @return {@code true} if watch mode is active
             */
            public boolean isWatch() {
                return watch;
            }

            /**
             * Sets whether file-watch hot-reload is enabled.
             *
             * @param watch {@code true} to enable watch mode
             */
            public void setWatch(boolean watch) {
                this.watch = watch;
            }
        }
    }

    /**
     * Facet type registry source selector ({@code mill.metadata.facet-type-registry}).
     */
    public static class FacetTypeRegistry {

        /**
         * Registry source strategy identifier.
         *
         * <p>Supported values:
         * <ul>
         *   <li>{@code inMemory} — in-process seeded facet type manifests (default fallback)</li>
         *   <li>{@code local}    — local persistence-backed repository (typically JPA)</li>
         *   <li>{@code portal}   — reserved for future remote descriptor source (not implemented)</li>
         * </ul>
         */
        private String type = "inMemory";

        /**
         * Returns the registry source strategy identifier.
         *
         * @return registry strategy type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the registry source strategy identifier.
         *
         * @param type registry strategy type
         */
        public void setType(String type) {
            this.type = type;
        }
    }
}
