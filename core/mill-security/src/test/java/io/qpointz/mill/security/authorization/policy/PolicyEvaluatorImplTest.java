package io.qpointz.mill.security.authorization.policy;

import io.qpointz.mill.security.authorization.policy.repositories.InMemoryPolicyRepository;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.qpointz.mill.security.authorization.policy.ActionVerb.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyEvaluatorImplTest {

    public record Action1(@Getter List<String> subject) implements Action {

        @Override
        public String actionName() {
            return "action-1";
        }
    }

    public record Action2(@Getter List<String> subject) implements Action {
        @Override
        public String actionName() {
            return "action-2";
        }
    }

    PolicyRepository simpleRepo() {
        return InMemoryPolicyRepository.builder().actions(List.of(
            new PolicyAction("POL1", ALLOW, new Action1(List.of("x","y","z"))),
            new PolicyAction("POL1", DENY, new Action1(List.of("x","y","z"))),
            new PolicyAction("POL2", ALLOW, new Action2(List.of("a","b","c"))),
            new PolicyAction("POL2", DENY, new Action1(List.of("a","b","c"))),
            new PolicyAction("POL3", ALLOW, new Action2(List.of("a","b","c"))),
            new PolicyAction("POL3", DENY, new Action1(List.of("a","b","c")))
        )).build();
    }

    @Test
    void selectAction() {
        val polSelector = mock(PolicySelector.class);
        when(polSelector.selectPolicies(any(),anySet())).thenAnswer(r-> Set.of("POL1"));
        val eval = new PolicyEvaluatorImpl(simpleRepo(), polSelector);
        val actions = eval.actions(k-> k.getPolicy().equals("POL1") && k.getVerb()== ALLOW);
        assertNotNull(actions);
        assertFalse(actions.isEmpty());
        val firstAction = actions.iterator().next();
        assertEquals(List.of("POL1", "allow", "action-1", "x","y","z"), firstAction.qualifiedId());
    }

    @Test
    void getListByType() {
        val polSelector = mock(PolicySelector.class);
        when(polSelector.selectPolicies(any(),anySet())).thenAnswer(r-> Set.of("POL1"));
        val eval = new PolicyEvaluatorImpl(simpleRepo(), polSelector);
        val actions = eval.actionsOf(Action1.class,
                k-> k.getPolicy().equals("POL1") && k.getVerb()== ALLOW,
                p-> p.subject().equals(List.of("x","y", "z")));
        assertNotNull(actions);
        assertEquals(1, actions.size());
    }

    @Test
    void getByVerbType() {
        val polSelector = mock(PolicySelector.class);
        when(polSelector.selectPolicies(any(),anySet())).thenAnswer(r-> Set.of("POL1"));
        val eval = new PolicyEvaluatorImpl(simpleRepo(), polSelector);
        val actions = eval.actionsOf(Action1.class, Set.of("POL3"), DENY, k-> k.getSubject().equals(List.of("a","b","c")));
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertEquals(List.of(new Action1(List.of("a","b","c"))), actions);
    }

    @Test
    void getActionsSet() {
        val polSelector = mock(PolicySelector.class);
        when(polSelector.selectPolicies(any(),anySet())).thenAnswer(r-> Set.of("POL1"));
        val eval = new PolicyEvaluatorImpl(simpleRepo(), polSelector);
        val pols = eval.actionsBy(Action2.class, Set.of("POL1", "POL3"), List.of("a","b","c"))
                .stream().toList();
        assertFalse(pols.isEmpty());
        assertEquals(List.of(new Action2(List.of("a","b","c"))), pols);
    }

}