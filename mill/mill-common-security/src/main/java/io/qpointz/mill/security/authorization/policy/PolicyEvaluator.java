package io.qpointz.mill.security.authorization.policy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PolicyEvaluator {

    <T extends Action> Collection<T> actionsBy(Class<T> collectionClass, ActionVerb verb,  List<String> subject);

}