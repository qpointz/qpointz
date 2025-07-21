package io.qpointz.mill.security.authorization.policy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.security.Policy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.qpointz.mill.security.authorization.policy.ActionVerb.ALLOW;
import static io.qpointz.mill.security.authorization.policy.ActionVerb.DENY;

@AllArgsConstructor
public class PolicyEvaluatorImpl implements PolicyEvaluator {

    @Getter(AccessLevel.PROTECTED)
    private final PolicyRepository repository;

    @Getter(AccessLevel.PROTECTED)
    private final PolicySelector policySelector;

    public Collection<PolicyAction> actions(Predicate<PolicyAction> policyActionPredicate) {
        return repository.actions().stream()
                .filter(policyActionPredicate)
                .toList();
    }

    public <T extends Action> Collection<T> actionsOf(Class<T> collectionClass, Predicate<PolicyAction> policyActionPredicate, Predicate<T> actionPredicate) {
        return this.actions(policyActionPredicate).stream()
                .filter(action -> collectionClass.isAssignableFrom(action.getAction().getClass()))
                .map(k-> collectionClass.cast(k.getAction()))
                .filter(actionPredicate)
                .toList();
    }

    public <T extends Action> Collection<T> actionsOf(Class<T> collectionClass, Set<String> policies, ActionVerb verb, Predicate<T> predicate) {
        return actionsOf(collectionClass,
                v-> (verb == null || v.getVerb() == verb) && (policies == null || policies.isEmpty() || policies.contains(v.getPolicy())),
                predicate
        );
    }

    public <T extends Action> Collection<T> actionsBy(Class<T> collectionClass, Set<String> allowPolicies, List<String> subject) {
        return actionsOf(collectionClass,
                pol -> (pol.getVerb() == ALLOW && allowPolicies.contains(pol.getPolicy())) ||
                        (pol.getVerb() == DENY  && ! allowPolicies.contains(pol.getPolicy())),
                act -> act.subject().equals(subject));
    }

    @Override
    public <T extends Action> Collection<T> actionsBy(Class<T> collectionClass, ActionVerb verb, List<String> subject) {
        val allPolicies = this.repository.actions().stream()
                .map(k-> k.getPolicy())
                .collect(Collectors.toSet());

        final Set<String> selectedPolicies = this.policySelector.selectPolicies(verb, allPolicies);

        return this.actionsOf(collectionClass,
                pol -> pol.getVerb() == verb && selectedPolicies.contains(pol.getPolicy()),
                act -> act.subject().equals(subject));
    }



}
