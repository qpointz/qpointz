package io.qpointz.mill.metadata.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration binding for metadata storage and import behaviour.
 *
 * <p>Bound to the {@code mill.metadata} prefix. Use the nested {@link Storage} class to
 * select the active backend ({@code file} by default) and the nested {@link File} class to
 * configure the file-backed repository path and hot-reload behaviour.
 *
 * <p>The {@code importOnStartup} field, when set, causes the application to import the named
 * classpath resource at startup via {@code MetadataImportService.import()} in MERGE mode.
 */
@ConfigurationProperties(prefix = "mill.metadata")
public class MetadataProperties {

    /** Storage backend selector; defaults to {@code new Storage()}. */
    private Storage storage = new Storage();

    /** File-storage configuration; defaults to {@code new File()}. */
    private File file = new File();

    /**
     * Optional classpath resource path for startup import.
     *
     * <p>When set (e.g. {@code classpath:metadata/seed.yaml}), the application imports the
     * named resource after the context is fully initialised.
     * The import runs in {@code MERGE} mode with {@code actorId="system"}.
     * Leave {@code null} (the default) to skip startup import.
     */
    private String importOnStartup;

    /**
     * Returns the storage configuration.
     *
     * @return current {@link Storage} settings
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Sets the storage configuration.
     *
     * @param storage replacement {@link Storage} instance
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     * Returns the file-storage configuration.
     *
     * @return current {@link File} settings
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file-storage configuration.
     *
     * @param file replacement {@link File} instance
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the classpath resource path used for startup import, or {@code null} if disabled.
     *
     * @return startup import resource path, or {@code null}
     */
    public String getImportOnStartup() {
        return importOnStartup;
    }

    /**
     * Sets the classpath resource path for startup import.
     *
     * @param importOnStartup resource path (e.g. {@code classpath:metadata/seed.yaml}),
     *                        or {@code null} to disable
     */
    public void setImportOnStartup(String importOnStartup) {
        this.importOnStartup = importOnStartup;
    }

    /**
     * Storage backend selector properties ({@code mill.metadata.storage}).
     */
    public static class Storage {

        /**
         * Backend type identifier.
         *
         * <p>Supported values:
         * <ul>
         *   <li>{@code file} — YAML file-backed repository (default)</li>
         *   <li>{@code jpa}  — relational JPA repository (requires {@code mill-metadata-persistence})</li>
         * </ul>
         */
        private String type = "file";

        /**
         * Returns the storage type identifier.
         *
         * @return backend type string
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the storage type identifier.
         *
         * @param type backend type string
         */
        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * File-storage specific configuration ({@code mill.metadata.file}).
     */
    public static class File {

        /**
         * Comma-separated list of Spring resource paths for YAML metadata files.
         *
         * <p>Supports classpath ({@code classpath:}) and filesystem ({@code file:}) prefixes,
         * as well as Ant-style glob patterns.
         * Defaults to {@code classpath:metadata/example.yml}.
         */
        private String path = "classpath:metadata/example.yml";

        /**
         * Whether to watch the backing files for changes and reload automatically.
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
