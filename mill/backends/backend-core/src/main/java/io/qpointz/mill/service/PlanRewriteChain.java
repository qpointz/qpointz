package io.qpointz.mill.service;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public final class PlanRewriteChain {

    @Getter
    private final List<PlanRewriter> rewriters;

}
