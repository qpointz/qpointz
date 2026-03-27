package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.calcite.CalciteConnectionContextBase;
import io.qpointz.mill.data.backend.calcite.CalciteContext;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.source.calcite.SourceSchemaManager;
import lombok.Getter;
import org.apache.calcite.jdbc.CalciteConnection;

import java.time.Duration;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * {@link CalciteContextFactory} that populates the Calcite root schema with
 * {@code FlowSchema} instances materialised from a {@link SourceDefinitionRepository}.
 */
public class FlowContextFactory implements CalciteContextFactory, AutoCloseable {

    @Getter
    private final SourceDefinitionRepository repository;

    @Getter
    private final Properties connectionProperties;
    @Getter
    private final boolean cacheSchemasEnabled;
    @Getter
    private final Duration cacheSchemasTtl;
    private final Object cacheLock = new Object();
    private volatile CachedSchemaEntry cachedSchemaEntry;

    private static class CachedSchemaEntry {
        private final SourceSchemaManager schemaManager;
        private final long createdAtNanos;

        private CachedSchemaEntry(SourceSchemaManager schemaManager, long createdAtNanos) {
            this.schemaManager = schemaManager;
            this.createdAtNanos = createdAtNanos;
        }
    }

    public FlowContextFactory(SourceDefinitionRepository repository, Properties connectionProperties) {
        this(repository, connectionProperties, false, null);
    }

    public FlowContextFactory(
            SourceDefinitionRepository repository,
            Properties connectionProperties,
            boolean cacheSchemas) {
        this(repository, connectionProperties, cacheSchemas, null);
    }

    public FlowContextFactory(
            SourceDefinitionRepository repository,
            Properties connectionProperties,
            boolean cacheSchemasEnabled,
            Duration cacheSchemasTtl) {
        this.repository = repository;
        this.connectionProperties = connectionProperties;
        this.cacheSchemasEnabled = cacheSchemasEnabled;
        this.cacheSchemasTtl = cacheSchemasTtl;
    }

    @Override
    public CalciteContext createContext() throws Exception {
        var schemaManager = cacheSchemasEnabled ? getOrRefreshCachedSchemaManager() : null;
        return new FlowConnectionContext(repository, connectionProperties, schemaManager);
    }

    @Override
    public void close() throws Exception {
        var entry = cachedSchemaEntry;
        cachedSchemaEntry = null;
        if (entry != null) {
            entry.schemaManager.close();
        }
    }

    private SourceSchemaManager getOrRefreshCachedSchemaManager() {
        var now = System.nanoTime();
        var existing = cachedSchemaEntry;
        if (existing != null && !isExpired(existing, now)) {
            return existing.schemaManager;
        }

        synchronized (cacheLock) {
            existing = cachedSchemaEntry;
            now = System.nanoTime();
            if (existing != null && !isExpired(existing, now)) {
                return existing.schemaManager;
            }
            var fresh = createSchemaManager(repository);
            cachedSchemaEntry = new CachedSchemaEntry(fresh, now);
            if (existing != null) {
                try {
                    existing.schemaManager.close();
                } catch (Exception ignored) {
                    // Ignore close failures for replaced cache entries.
                }
            }
            return fresh;
        }
    }

    private boolean isExpired(CachedSchemaEntry entry, long nowNanos) {
        if (cacheSchemasTtl == null) {
            return false;
        }
        if (cacheSchemasTtl.isZero() || cacheSchemasTtl.isNegative()) {
            return true;
        }
        return nowNanos - entry.createdAtNanos >= cacheSchemasTtl.toNanos();
    }

    private static SourceSchemaManager createSchemaManager(SourceDefinitionRepository repository) {
        var schemaManager = new SourceSchemaManager();
        for (var descriptor : repository.getSourceDefinitions()) {
            schemaManager.add(descriptor);
        }
        return schemaManager;
    }

    private static class FlowConnectionContext extends CalciteConnectionContextBase {

        @Getter
        private final CalciteConnection calciteConnection;
        private final SourceSchemaManager schemaManager;
        private final boolean ownsSchemaManager;

        FlowConnectionContext(
                SourceDefinitionRepository repository,
                Properties props,
                SourceSchemaManager cachedSchemaManager) throws Exception {
            Class.forName("org.apache.calcite.jdbc.Driver");
            this.calciteConnection = DriverManager
                    .getConnection("jdbc:calcite:", props)
                    .unwrap(CalciteConnection.class);

            if (cachedSchemaManager != null) {
                this.schemaManager = cachedSchemaManager;
                this.ownsSchemaManager = false;
            } else {
                this.schemaManager = createSchemaManager(repository);
                this.ownsSchemaManager = true;
            }
            schemaManager.registerAll(getRootSchema());
        }

        @Override
        public void close() throws Exception {
            try {
                if (ownsSchemaManager) {
                    schemaManager.close();
                }
            } finally {
                calciteConnection.close();
            }
        }
    }
}
