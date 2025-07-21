package io.qpointz.flow.records;

import io.qpointz.mill.types.logical.LogicalType;

public record RecordAttribute(String name, int index, LogicalType<?,?> typeId) {

}
