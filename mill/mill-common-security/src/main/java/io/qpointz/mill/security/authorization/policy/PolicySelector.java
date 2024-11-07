package io.qpointz.mill.security.authorization.policy;

import java.util.Set;

/**
 * Selects policies by verb dependent on current context e.g. based on current user authentication
 * Most trivial selecting policies based on GrantedAuthorities
 */
public interface PolicySelector {

    /**
     * @param verb verb policy must be selected for
     * @param policySet full set of in scope policies
     * @return subset of in-scope policies matching verb to current context
     */
    Set<String> selectPolicies(ActionVerb verb, Set<String> policySet);

}
