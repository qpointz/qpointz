package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.types.logical.*;

public class VectorColumnReaderFactory extends LogicalTypeIdMapper<ColumnReader> {

    private final Vector vector;
    private final Field field;

    public VectorColumnReaderFactory(Field field, Vector vector) {
        this.field = field;
        this.vector = vector;
    }

    @Override
    protected ColumnReader mapUUID() {
        return new UUIDColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapTime() {
        return new TimeColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapTimestampTZ() {
        return new TimestampTzColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapTimestamp() {
        return new TimestampColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapString() {
        return new StringColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapIntervalYear(){
        return new IntervalYearColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapIntervalDay(){
        return new IntervalDayColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapDouble(){
        return new DoubleColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapFloat(){
        return new FloatColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapDate(){
        return new DateColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapBool() {
        return new BoolColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapBinary() {
        return new BinaryColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapInt() {
        return new IntColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapSmallInt() {
        return new SmallIntColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapTinyInt() {
        return new TinyIntColumnVectorReader(this.vector);
    }

    @Override
    protected ColumnReader mapBigInt() {
        return new BigIntColumnVectorReader(this.vector);
    }
}
