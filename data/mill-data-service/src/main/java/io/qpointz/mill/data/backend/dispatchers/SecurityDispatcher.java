package io.qpointz.mill.data.backend.dispatchers;

import java.util.Collection;

public interface SecurityDispatcher {

    String principalName();

    Collection<String> authorities();

}
