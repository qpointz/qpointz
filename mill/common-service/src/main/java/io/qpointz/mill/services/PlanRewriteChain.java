package io.qpointz.mill.services;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public final class PlanRewriteChain {

    @Getter
    private final List<PlanRewriter> rewriters;

}
