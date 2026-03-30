package io.qpointz.mill.metadata.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for ordered startup metadata seed resources (SPEC §14.1).
 *
 * <p>Bound to {@code mill.metadata.seed}. Each resource is imported via {@link io.qpointz.mill.metadata.service.MetadataImportService#import}
 * in {@link io.qpointz.mill.metadata.domain.ImportMode#MERGE} mode with actor {@code system}.
 * Completion is recorded in {@code metadata_seed} when a {@link io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository} bean is present.
 * Ledger keys are derived from the resource location (canonical file URI when applicable), not from the list index, so reordering {@code resources} does not re-trigger completed seeds.
 * After each successful import, an {@code md5:} hex fingerprint of the raw seed bytes is stored; if the file content changes, the seed runs again (merge) and the ledger row is updated.
 */
@ConfigurationProperties(prefix = "mill.metadata.seed")
public class MetadataSeedProperties {

    /**
     * Ordered Spring {@link org.springframework.core.io.Resource} locations (e.g. {@code classpath:metadata/01-base.yaml}).
     * Execution order matches list order. Empty list skips the seed runner.
     */
    private List<String> resources = new ArrayList<>();

    /**
     * Behaviour when a seed resource fails. {@code fail-fast} (default) stops startup; {@code continue} logs and proceeds.
     */
    private String onFailure = "fail-fast";

    /**
     * Returns the ordered seed resource locations.
     *
     * @return modifiable list of locations
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * Sets the ordered seed resource locations.
     *
     * @param resources replacement list
     */
    public void setResources(List<String> resources) {
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    /**
     * Returns the failure policy token.
     *
     * @return {@code fail-fast} or {@code continue}
     */
    public String getOnFailure() {
        return onFailure;
    }

    /**
     * Sets the failure policy token.
     *
     * @param onFailure {@code fail-fast} or {@code continue}
     */
    public void setOnFailure(String onFailure) {
        this.onFailure = onFailure;
    }
}
