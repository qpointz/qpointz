package io.qpointz.mill.services;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class Service {

    @Getter(AccessLevel.PROTECTED)
    private final ServiceHandler serviceHandler;

    protected Service (ServiceHandler context) {
        this.serviceHandler = context;
    }

}
