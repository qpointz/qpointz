package io.qpointz.mill.security.authorization.policy.repositories;

import io.qpointz.mill.security.authorization.policy.PolicyAction;
import io.qpointz.mill.security.authorization.policy.PolicyActionDescriptor;
import io.qpointz.mill.security.authorization.policy.PolicyRepository;
import io.qpointz.mill.security.authorization.policy.actions.ExpressionFilterAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.Collection;

@AllArgsConstructor
public class PolicyActionDescriptorRepository implements PolicyRepository {

    @Getter
    Collection<PolicyActionDescriptor> descriptors;

    @Override
    public Collection<PolicyAction> actions() {
        return descriptors.stream()
                .map(this::fromDescriptor)
                .toList();
    }

    private PolicyAction fromDescriptor(PolicyActionDescriptor k) {
        if (k.getAction() == null || k.getAction().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty:"+k.toString());
        }
        val actionBuilder = PolicyAction.builder()
                .policy(k.getPolicy())
                .verb(k.getVerb());
        val actionName = k.getAction().toLowerCase();

        if (actionName.equals("rel-filter")) {
            return actionBuilder
                    .action(ExpressionFilterAction.fromDescriptor(k))
                    .build();
        }

        throw new IllegalArgumentException("Unknown action:"+actionName);
    }
}
