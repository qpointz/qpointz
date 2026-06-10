package io.qpointz.mill.ai.nlsql.processors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record QueryResult(DataContainer container) {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "container-type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubmitQueryProcessor.PagingResult.class, name = "paging"),
            @JsonSubTypes.Type(value = ExecuteQueryProcessor.ExecutionResult.class, name = "data")
    })
    public interface DataContainer {}

}
