package io.qpointz.mill.security.authorization.policy;

import io.qpointz.mill.services.dispatchers.SecurityDispatcher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.qpointz.mill.security.authorization.policy.ActionVerb.ALLOW;
import static io.qpointz.mill.security.authorization.policy.ActionVerb.DENY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GrantedAuthoritiesPolicySelectorTest {

    @AllArgsConstructor
    public static class NamedGrantedAuthority implements GrantedAuthority {

        @Getter
        private String authority;

    }

    @Test
    void trivia() {
        val dispatcher = mock(SecurityDispatcher.class);
        val auths = List.of(
                 new NamedGrantedAuthority("a"),
                 new NamedGrantedAuthority("b"),
                 new NamedGrantedAuthority("c"));
        when(dispatcher.grantedAuthorities()).thenAnswer(invocation -> auths);
        val g = new GrantedAuthoritiesPolicySelector(dispatcher, Map.of());
        val allow = g.selectPolicies(ALLOW, Set.of("a", "c", "z"));
        assertEquals(Set.of("a","c"), allow);

        val deny = g.selectPolicies(DENY, Set.of("z", "a"));
        assertEquals(Set.of("z"), deny);
    }

    @Test
    void failsOnUnknown() {
        val dispatcher = mock(SecurityDispatcher.class);
        val auths = List.of(
                new NamedGrantedAuthority("a"),
                new NamedGrantedAuthority("b"),
                new NamedGrantedAuthority("c"));
        when(dispatcher.grantedAuthorities()).thenAnswer(invocation -> auths);
        val g = new GrantedAuthoritiesPolicySelector(dispatcher, Map.of());
        assertThrows(IllegalArgumentException.class, ()-> g.selectPolicies(null, Set.of("a", "c", "z")));
    }

    @Test
    void authRemap() {
        val dispatcher = mock(SecurityDispatcher.class);
        val auths = List.of(
                new NamedGrantedAuthority("a1"),
                new NamedGrantedAuthority("b"),
                new NamedGrantedAuthority("c1"));
        when(dispatcher.grantedAuthorities()).thenAnswer(invocation -> auths);
        val g = new GrantedAuthoritiesPolicySelector(dispatcher, Map.of("a1", "a"));
        //val verb = ActionVerb.valueOf("döslfkldö");
        val allow = g.selectPolicies(ALLOW, Set.of("a", "b"));
        assertEquals(Set.of("a","b"), allow);
    }





}