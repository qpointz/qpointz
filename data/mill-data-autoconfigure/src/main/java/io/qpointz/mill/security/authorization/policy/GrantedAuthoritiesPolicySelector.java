package io.qpointz.mill.security.authorization.policy;

import io.qpointz.mill.services.dispatchers.SecurityDispatcher;
import lombok.val;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GrantedAuthoritiesPolicySelector implements PolicySelector {

    private final SecurityDispatcher securityDispatcher;
    private final Map<String, String> remap;

    public GrantedAuthoritiesPolicySelector(SecurityDispatcher dispatcher, Map<String, String> remap) {
        this.securityDispatcher = dispatcher;
        this.remap = remap != null ? remap : Map.of();
    }

    @Override
    public Set<String> selectPolicies(ActionVerb verb, Set<String> policySet) {
        val groups = this.securityDispatcher.authorities().stream()
                .map(k -> this.remap.getOrDefault(k, k))
                .collect(Collectors.toSet());

        if (verb == ActionVerb.ALLOW) {
            return policySet.stream()
                    .filter(groups::contains)
                    .collect(Collectors.toSet());
        }

        if (verb == ActionVerb.DENY) {
            return policySet.stream()
                    .filter(k -> !groups.contains(k))
                    .collect(Collectors.toSet());
        }

        throw new IllegalArgumentException("Unsupported verb: " + verb);

    }
}
