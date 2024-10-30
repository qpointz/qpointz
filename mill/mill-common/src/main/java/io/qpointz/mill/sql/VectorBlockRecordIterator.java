package io.qpointz.mill.sql;

import io.qpointz.mill.MillRuntimeDataException;
import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.sql.readers.vector.ColumnMetadata;
import io.qpointz.mill.sql.readers.vector.ColumnMetadataFactory;
import io.qpointz.mill.sql.readers.vector.VectorColumnReaderFactory;
import lombok.Getter;
import lombok.val;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.BiFunction;

public abstract class VectorBlockRecordIterator implements RecordReader {

    private final Iterator<VectorBlock> vectorBlocks ;
    private int rowIdx = -1;
    private VectorBlock vectorBlock = null;

    protected VectorBlockRecordIterator(Iterator<VectorBlock> vectorBlocks) {
      this.vectorBlocks = vectorBlocks;
    }


    @Override
    public boolean hasNext() {
        if (this.vectorBlock == null) {
            if (this.vectorBlocks.hasNext()) {
                this.reset();
                return hasNext();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean next() {
        if (this.vectorBlock == null) {
            if (this.vectorBlocks.hasNext()) {
                reset();
                return next();
            } else {
                return false;
            }
        }

        val newRowIdx = this.rowIdx+1;
        if (newRowIdx>= this.vectorBlock.getVectorSize()) {
            this.vectorBlock = null;
            return this.next();
        }

        this.rowIdx = newRowIdx;
        return true;
    }

    private void reset() {
        val vBlock = this.vectorBlocks.next();
        this.vectorBlock = vBlock;
        this.rowIdx = -1;
        val readers = new ArrayList<ColumnReader>();
        val metadata = new ArrayList<ColumnMetadata>();
        val names = new HashMap<String,Integer>();

        val schema = vBlock.getSchema();
        for (var i=0;i<schema.getFieldsCount();i++) {
            val field = schema.getFields(i);
            val logicalType = field.getType().getType();
            val columnReader = new VectorColumnReaderFactory(vBlock.getVectors(i)).byId(logicalType.getTypeId());
            readers.add(columnReader);

            val cMeta = new ColumnMetadataFactory(field).byId(logicalType.getTypeId());
            metadata.add(cMeta);

            names.put(field.getName(), field.getFieldIdx());
        }
        this.columnReaders = readers;
        this.columnMetadata = metadata;
        this.columnNames = names;
    }

    @Getter
    private List<ColumnReader> columnReaders = null;

    @Getter
    private List<ColumnMetadata> columnMetadata;

    @Getter
    private Map<String, Integer> columnNames;

    private ColumnReader columnReader(int columnIndex) {
        return this.getColumnReaders().get(columnIndex);
    }

    @Override
    public boolean isNull(int columnIndex) {
        return this.columnReader(columnIndex).isNull(rowIdx);
    }

    private enum ColumnReaderOp {
        GET_BOOLEAN,
        GET_BYTE,
        GET_SHORT,
        GET_INT,
        GET_LONG,
        GET_FLOAT,
        GET_DOUBLE,
        GET_BIG_DECIMAL,
        GET_BYTES,
        GET_DATE,
        GET_TIME,
        GET_TIMESTAMP,
        GET_ASCII_STREAM,
        GET_UNICODE_STREAM,
        GET_BINARY_STREAM,
        GET_OBJECT,
        GET_STRING
    }

    private <T> T doColumnReaderOp(ColumnReaderOp op, int columnIdx, int rowIdx, BiFunction<ColumnReader, Integer, T> getVal) {
        try {
            val columnReader = this.columnReader(columnIdx);
            return getVal.apply(columnReader, rowIdx);
        } catch (Exception ex) {
            val column = this.vectorBlock.getSchema().getFields(columnIdx);
            val msg = String.format("Failed operation '%s' on column %s: %s (%s)",op.name(), columnIdx, column.getName(), column.getType().getType().getTypeId().name());
            throw new MillRuntimeDataException(msg, ex);
        }
    }

    @Override
    public String getString(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_STRING,
                columnIndex, rowIdx,
                ColumnReader::getString);
    }

    @Override
    public Boolean getBoolean(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_BOOLEAN,
                columnIndex, rowIdx,
                ColumnReader::getBoolean);
    }

    @Override
    public byte getByte(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_BYTE,
                columnIndex, rowIdx,
                ColumnReader::getByte);
    }

    @Override
    public short getShort(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_SHORT,
                columnIndex, rowIdx,
                ColumnReader::getShort);
    }

    @Override
    public int getInt(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_INT,
                columnIndex, rowIdx,
                ColumnReader::getInt);
    }

    @Override
    public long getLong(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_LONG,
                columnIndex, rowIdx,
                ColumnReader::getLong);
    }

    @Override
    public float getFloat(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_FLOAT,
                columnIndex, rowIdx,
                ColumnReader::getFloat);
    }

    @Override
    public double getDouble(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_DOUBLE,
                columnIndex, rowIdx,
                ColumnReader::getDouble);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_BIG_DECIMAL,
                columnIndex, rowIdx,
                ColumnReader::getBigDecimal);
    }

    @Override
    public byte[] getBytes(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_BYTES,
                columnIndex, rowIdx,
                ColumnReader::getBytes);
    }

    @Override
    public Date getDate(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_DATE,
                columnIndex, rowIdx,
                ColumnReader::getDate);
    }

    @Override
    public Time getTime(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_TIME,
                columnIndex, rowIdx,
                ColumnReader::getTime);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_TIMESTAMP,
                columnIndex, rowIdx,
                ColumnReader::getTimestamp);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_ASCII_STREAM,
                columnIndex, rowIdx,
                ColumnReader::getAsciiString);
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_UNICODE_STREAM,
                columnIndex, rowIdx,
                ColumnReader::getUnicodeString);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_BINARY_STREAM,
                columnIndex, rowIdx,
                ColumnReader::getBinaryStream);
    }

    @Override
    public Object getObject(int columnIndex) {
        return doColumnReaderOp(ColumnReaderOp.GET_OBJECT,
                columnIndex, rowIdx,
                ColumnReader::getObject);
    }

    @Override
    public int getColumnIndex(String columnLabel) {
        return this.getColumnNames().get(columnLabel);
    }

    @Override
    public int getColumnCount() {
        return this.getColumnMetadata().size();
    }

    @Override
    public ColumnMetadata getColumnMetadata(int columnIndex) {
        return this.getColumnMetadata().get(columnIndex);
    }

    public static VectorBlockRecordIterator of(VectorBlock block) {
        return of(List.of(block).iterator());
    }

    public static VectorBlockRecordIterator of(Iterator<VectorBlock> blocks) {
        return new VectorBlockRecordIterator(blocks) {
            @Override
            public void close() {
                //no closable resources associtated with record iterator
            }
        };
    }

}
