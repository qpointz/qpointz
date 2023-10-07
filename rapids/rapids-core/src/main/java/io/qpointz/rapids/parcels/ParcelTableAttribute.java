package io.qpointz.rapids.parcels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class ParcelTableAttribute {

    @Getter
    public String name;

    @Getter
    public ParcelDataType dataType;

}
