package io.qpointz.mill.types.conversion;

import lombok.val;

import java.nio.ByteBuffer;
import java.util.UUID;

public class BinaryToUUIDConverter implements ValueConverter<UUID, byte[]> {

    public static final BinaryToUUIDConverter DEFAULT = new BinaryToUUIDConverter();

    @Override
    public byte[] to(UUID value) {
        val bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(value.getMostSignificantBits());
        bb.putLong(value.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public UUID from(byte[] value) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}
