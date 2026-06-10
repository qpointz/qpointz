package io.qpointz.mill.ai.scenarios.actions.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for check implementations.
 * Maps check keys to their corresponding Check implementations.
 */
public class CheckRegistry {
    private final Map<String, Check> checks = new HashMap<>();

    /**
     * Creates a new registry with the default built-in checks.
     */
    public CheckRegistry() {
        registerCheck(new IntentCheck());
        registerCheck(new HasCheck());
        registerCheck(new SqlShapeCheck());
        registerCheck(new ReturnsCheck());
        registerCheck(new EnrichmentCheck());
    }

    /**
     * Gets a check by its key.
     *
     * @param key the check key
     * @return Optional containing the check if found, empty otherwise
     */
    public Optional<Check> getCheck(String key) {
        return Optional.ofNullable(checks.get(key));
    }

    /**
     * Registers a check implementation.
     * If a check with the same key already exists, it will be replaced.
     *
     * @param check the check to register
     */
    public void registerCheck(Check check) {
        if (check == null) {
            throw new IllegalArgumentException("Check cannot be null");
        }
        checks.put(check.getCheckKey(), check);
    }
}

