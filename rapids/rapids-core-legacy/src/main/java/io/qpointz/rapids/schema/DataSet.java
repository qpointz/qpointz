package io.qpointz.rapids.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
public class DataSet {

    @Getter
    private final Namespace namespace;

    @Getter
    private final String name;

    @Getter
    private final List<Attribute> attributes;


}
