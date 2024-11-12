package io.qpointz.mill.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ServiceHandlerPlanRewriteContext implements PlanRewriteContext {

    @Getter
    private final ServiceHandler serviceHandler;

}
