package io.qpointz.mill.security.authorization.policy;

import java.util.Collection;

public interface PolicyRepository {

    Collection<PolicyAction> actions();

}