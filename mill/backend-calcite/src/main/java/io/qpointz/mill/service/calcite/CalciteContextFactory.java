package io.qpointz.mill.service.calcite;

import org.apache.calcite.rel.core.Calc;

public interface CalciteContextFactory {

    CalciteContext createContext() throws Exception;

}
