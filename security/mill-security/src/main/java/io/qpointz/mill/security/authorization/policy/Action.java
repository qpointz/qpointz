package io.qpointz.mill.security.authorization.policy;

import java.util.List;

public interface Action {

    List<String> subject();

    String actionName();
}
