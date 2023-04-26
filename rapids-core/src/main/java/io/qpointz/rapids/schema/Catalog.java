package io.qpointz.rapids.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Builder
public class Catalog {

    @Getter
    private final Collection<DataSet> dataSets;

}
