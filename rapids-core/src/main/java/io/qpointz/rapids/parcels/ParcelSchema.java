package io.qpointz.rapids.parcels;


import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.HashMap;
import java.util.Map;

public class ParcelSchema extends AbstractSchema {

    private final Parcel parcel;

    protected ParcelSchema(SchemaPlus parentSchema, Parcel parcel) {
        super();
        this.parcel = parcel;
    }

    @Override
    public Map<String, Table> getTableMap() {
        final var parcelDatasets = parcel.getTables();
        final var tables = new HashMap<String, Table>();
        for (final var dataSet : parcelDatasets.entrySet()) {
            tables.put(dataSet.getKey(), dataSet.getValue());
        }
        return tables;
    }

}
