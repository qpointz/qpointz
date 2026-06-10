package io.qpointz.mill.ai.scenarios.actions.checks;

import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnrichmentCheck extends AbstractCheck {

    @Override
    public String getCheckKey() {
        return "enrichment";
    }

    @Override
    public List<CheckResult> execute(Map<String, Object> checkParams, Map<String, Object> result) {
        val enrichments = ((List<?>)result
                .getOrDefault("enrichment", List.<Map<String,Object>>of()))
                .stream().map(k-> (Map<String,Object>)k)
                .toList();

        val results = new ArrayList<CheckResult>();

        if (checkParams.containsKey("types")) {
            val existingTypes = enrichments.stream()
                    .filter(k-> k.containsKey("type"))
                    .map(k-> k.get("type").toString())
                    .collect(Collectors.toSet());

            val checkTypes = ((List<?>) checkParams.getOrDefault("types", List.class))
                    .stream().map(k -> k.toString())
                    .collect(Collectors.toSet());

            if (existingTypes.equals(checkTypes)) {
                results.add(new CheckResult(ActionOutcome.OutcomeStatus.PASS,
                        "verify-enrichment-types", "Expected enrichment types: passing", checkParams));
            } else if (existingTypes.isEmpty()) {
                results.add(new CheckResult(ActionOutcome.OutcomeStatus.ERROR,
                        "verify-enrichment-types", "No enrichments generated", checkParams));
            }

        }

        return results;
    }
}
