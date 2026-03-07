package io.qpointz.mill.metadata.database;

import io.qpointz.mill.metadata.ResultSetProvidingMetadata;
import io.qpointz.mill.proto.DialectDescriptor;
import io.qpointz.mill.types.logical.BoolLogical;
import io.qpointz.mill.types.logical.IntLogical;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.qpointz.mill.metadata.database.MetadataUtils.integerOf;
import static io.qpointz.mill.metadata.database.MetadataUtils.stringOf;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

public class TypeInfoMetadata extends ResultSetProvidingMetadata<TypeInfoMetadata.TypeInfoRecord> {

    private final List<DialectDescriptor.TypeInfo> typeInfoEntries;

    public TypeInfoMetadata(List<DialectDescriptor.TypeInfo> typeInfoEntries) {
        this.typeInfoEntries = typeInfoEntries == null ? List.of() : typeInfoEntries;
    }

    protected record TypeInfoRecord(
            String typeName,
            Integer dataType,
            Integer precision,
            String literalPrefix,
            String literalSuffix,
            Integer nullable,
            Boolean caseSensitive,
            Integer searchable,
            Boolean unsignedAttribute,
            Boolean fixedPrecScale,
            Boolean autoIncrement,
            String localTypeName,
            Integer minimumScale,
            Integer maximumScale,
            Integer sqlDataType,
            Integer sqlDatetimeSub,
            Integer numPrecRadix
    ) {
    }

    private static final List<ObjectToVectorProducer.MapperInfo<TypeInfoRecord, ?>> RECORD_SET_FIELD_MAPPINGS = List.of(
            mapper("TYPE_NAME", StringLogical.INSTANCE, k -> stringOf(k.typeName)),
            mapper("DATA_TYPE", IntLogical.INSTANCE, k -> integerOf(k.dataType)),
            mapper("PRECISION", IntLogical.INSTANCE, k -> integerOf(k.precision)),
            mapper("LITERAL_PREFIX", StringLogical.INSTANCE, k -> stringOf(k.literalPrefix)),
            mapper("LITERAL_SUFFIX", StringLogical.INSTANCE, k -> stringOf(k.literalSuffix)),
            mapper("CREATE_PARAMS", StringLogical.INSTANCE, k -> Optional.empty()),
            mapper("NULLABLE", IntLogical.INSTANCE, k -> integerOf(k.nullable)),
            mapper("CASE_SENSITIVE", BoolLogical.INSTANCE, k -> optionalBool(k.caseSensitive)),
            mapper("SEARCHABLE", IntLogical.INSTANCE, k -> integerOf(k.searchable)),
            mapper("UNSIGNED_ATTRIBUTE", BoolLogical.INSTANCE, k -> optionalBool(k.unsignedAttribute)),
            mapper("FIXED_PREC_SCALE", BoolLogical.INSTANCE, k -> optionalBool(k.fixedPrecScale)),
            mapper("AUTO_INCREMENT", BoolLogical.INSTANCE, k -> optionalBool(k.autoIncrement)),
            mapper("LOCAL_TYPE_NAME", StringLogical.INSTANCE, k -> stringOf(k.localTypeName)),
            mapper("MINIMUM_SCALE", IntLogical.INSTANCE, k -> integerOf(k.minimumScale)),
            mapper("MAXIMUM_SCALE", IntLogical.INSTANCE, k -> integerOf(k.maximumScale)),
            mapper("SQL_DATA_TYPE", IntLogical.INSTANCE, k -> Optional.empty()),
            mapper("SQL_DATETIME_SUB", IntLogical.INSTANCE, k -> Optional.empty()),
            mapper("NUM_PREC_RADIX", IntLogical.INSTANCE, k -> integerOf(k.numPrecRadix))
    );

    @Override
    protected List<ObjectToVectorProducer.MapperInfo<TypeInfoRecord, ?>> getMappers() {
        return RECORD_SET_FIELD_MAPPINGS;
    }

    @Override
    protected Collection<TypeInfoRecord> getMetadata() {
        return typeInfoEntries.stream()
                .map(TypeInfoMetadata::toRecord)
                .toList();
    }

    private static Optional<Boolean> optionalBool(Boolean value) {
        return Optional.ofNullable(value);
    }

    private static TypeInfoRecord toRecord(DialectDescriptor.TypeInfo entry) {
        return new TypeInfoRecord(
                entry.getSqlName(),
                entry.getJdbcTypeCode(),
                entry.hasPrecision() ? entry.getPrecision() : null,
                entry.hasLiteralPrefix() ? entry.getLiteralPrefix() : null,
                entry.hasLiteralSuffix() ? entry.getLiteralSuffix() : null,
                DatabaseMetaData.typeNullableUnknown,
                entry.hasCaseSensitive() ? entry.getCaseSensitive() : null,
                entry.hasSearchable() ? entry.getSearchable() : null,
                entry.hasUnsigned() ? entry.getUnsigned() : null,
                entry.hasFixedPrecScale() ? entry.getFixedPrecScale() : null,
                entry.hasAutoIncrement() ? entry.getAutoIncrement() : null,
                null,
                entry.hasMinimumScale() ? entry.getMinimumScale() : null,
                entry.hasMaximumScale() ? entry.getMaximumScale() : null,
                null,
                null,
                entry.hasNumPrecRadix() ? entry.getNumPrecRadix() : null
        );
    }
}
