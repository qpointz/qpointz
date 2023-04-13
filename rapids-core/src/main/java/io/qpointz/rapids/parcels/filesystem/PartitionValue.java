package io.qpointz.rapids.parcels.filesystem;

import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PartitionValue {

    @Getter
    private final PartitionValueType valueType;

    @Getter
    private final String key;

    @Getter
    private final Object value;

    public String getString() {
        return this.getValue().toString();
    }

    public int getInt() {
        return (int)this.getValue();
    }

}
