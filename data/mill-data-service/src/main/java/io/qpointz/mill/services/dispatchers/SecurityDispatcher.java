package io.qpointz.mill.services.dispatchers;

import java.util.Collection;

public interface SecurityDispatcher {

    String principalName();

    Collection<String> authorities();

}
