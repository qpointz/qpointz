package io.qpointz.mill.security.authorization.policy.repositories;

import io.qpointz.mill.security.authorization.policy.PolicyAction;
import io.qpointz.mill.security.authorization.policy.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Collection;

@AllArgsConstructor
@Builder
public final class InMemoryPolicyRepository implements PolicyRepository {

    @Getter
    @Accessors(fluent = true)
    private final Collection<PolicyAction> actions;

}
