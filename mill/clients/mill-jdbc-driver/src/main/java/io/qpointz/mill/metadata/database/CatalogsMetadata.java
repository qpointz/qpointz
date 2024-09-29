package io.qpointz.mill.metadata.database;

import io.qpointz.mill.metadata.ResultSetProvidingMetadata;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;

import java.util.Collection;
import java.util.List;

import static io.qpointz.mill.metadata.database.MetadataUtils.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

public class CatalogsMetadata extends ResultSetProvidingMetadata<CatalogsMetadata.CatalogRecord> {

    public static CatalogsMetadata DEFAULT = new CatalogsMetadata();

    protected record CatalogRecord(String catalog) {};

    private static List<ObjectToVectorProducer.MapperInfo<CatalogsMetadata.CatalogRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CAT", StringLogical.INSTANCE, k-> dbnull())
    );

    @Override
    protected List<ObjectToVectorProducer.MapperInfo<CatalogRecord, ?>> getMappers() {
        return MAPPINGS;
    }

    @Override
    protected Collection<CatalogRecord> getMetadata() {
        return List.of();
    }

}
