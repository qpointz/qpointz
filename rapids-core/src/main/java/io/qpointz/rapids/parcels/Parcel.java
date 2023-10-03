package io.qpointz.rapids.parcels;

import java.util.Map;

public interface Parcel {

    String getName();

    void init() ;

    void refresh() ;

    Map<String, ParcelTable> getTables();
}
