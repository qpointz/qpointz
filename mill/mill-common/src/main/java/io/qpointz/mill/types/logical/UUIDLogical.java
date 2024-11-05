package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.types.conversion.BinaryToUUIDConverter;
import io.qpointz.mill.types.physical.BytesPhysical;

import java.util.UUID;

public final class UUIDLogical implements LogicalType<byte[], BytesPhysical> {

    private UUIDLogical() {}

    public static final UUIDLogical INSTANCE = new UUIDLogical();

    @Override
    public <T> T accept(LogicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    @Override
    public BytesPhysical getPhysicalType() {
        return BytesPhysical.INSTANCE;
    }

    @Override
    public LogicalDataType.LogicalDataTypeId getLogicalTypeId() {
        return LogicalDataType.LogicalDataTypeId.UUID;
    }

    public static final BinaryToUUIDConverter DEFAULT_CONVERTER = new BinaryToUUIDConverter();

    public static byte[] toPhysical(UUID uuid) {
        return DEFAULT_CONVERTER.to(uuid);
    }

    public static UUID fromPhysical(byte[] bytes) {
        return DEFAULT_CONVERTER.from(bytes);
    }

}
