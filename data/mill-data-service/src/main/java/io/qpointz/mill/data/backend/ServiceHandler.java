package io.qpointz.mill.data.backend;

import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.dispatchers.ResultAllocator;
import io.qpointz.mill.data.backend.dispatchers.SecurityDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceHandler {

//    @Getter
//    private final MetadataProvider metadataProvider;

//    @Getter
//    private final ExecutionProvider executionProvider;

//    @Getter
//    private final SqlProvider sqlProvider;

//    @Getter
//    private final PlanRewriteChain planRewriteChain;


    private final SubstraitDispatcher substraitDispatcher;
    private final SecurityDispatcher securityDispatcher;
    private final DataOperationDispatcher dataDispatcher;

    public ServiceHandler(DataOperationDispatcher dataOperationDispatcher,
                          SecurityDispatcher securityDispatcher,
                          ResultAllocator resultAllocator,
                          SubstraitDispatcher substraitDispatcher) {
        //this.metadataProvider = metadataProvider;
        //this.executionProvider = executionProvider;
        //this.sqlProvider = sqlProvider;

        //this.planRewriteChain = planRewriteChain;
        this.substraitDispatcher = substraitDispatcher;
        this.securityDispatcher = securityDispatcher;
        this.dataDispatcher = dataOperationDispatcher;


    }

    public SecurityDispatcher security() {
        return this.securityDispatcher;
    }

    public DataOperationDispatcher data() {
        return this.dataDispatcher;
    }

    public SubstraitDispatcher substrait() {
        return this.substraitDispatcher;
    }





}
