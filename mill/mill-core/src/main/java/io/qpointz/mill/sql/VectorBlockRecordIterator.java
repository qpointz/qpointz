package io.qpointz.mill.sql;

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
            val columnReader = new VectorColumnReaderFactory(field, vBlock.getVectors(i)).byId(logicalType.getTypeId());
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

    @Override
    public String getString(int columnIndex) {
        return this.columnReader(columnIndex).getString(rowIdx);
    }

    @Override
    public Boolean getBoolean(int columnIndex) {
        return this.columnReader(columnIndex).getBoolean(rowIdx);
    }

    @Override
    public byte getByte(int columnIndex) {
        return this.columnReader(columnIndex).getByte(rowIdx);
    }

    @Override
    public short getShort(int columnIndex) {
        return this.columnReader(columnIndex).getShort(rowIdx);
    }

    @Override
    public int getInt(int columnIndex) {
        return this.columnReader(columnIndex).getInt(rowIdx);
    }

    @Override
    public long getLong(int columnIndex) {
        return this.columnReader(columnIndex).getLong(rowIdx);
    }

    @Override
    public float getFloat(int columnIndex) {
        return this.columnReader(columnIndex).getFloat(rowIdx);
    }

    @Override
    public double getDouble(int columnIndex) {
        return this.columnReader(columnIndex).getDouble(rowIdx);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {
        return this.columnReader(columnIndex).getBigDecimal(rowIdx);
    }

    @Override
    public byte[] getBytes(int columnIndex) {
        return this.columnReader(columnIndex).getBytes(rowIdx);
    }

    @Override
    public Date getDate(int columnIndex) {
        return this.columnReader(columnIndex).getDate(rowIdx);
    }

    @Override
    public Time getTime(int columnIndex) {
        return this.columnReader(columnIndex).getTime(rowIdx);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) {
        return this.columnReader(columnIndex).getTimestamp(rowIdx);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) {
        return this.columnReader(columnIndex).getAsciiString(rowIdx);
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) {
        return this.columnReader(columnIndex).getUnicodeString(rowIdx);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) {
        return this.columnReader(columnIndex).getBinaryStream(rowIdx);
    }

    @Override
    public Object getObject(int columnIndex) {
        return this.columnReader(columnIndex).getObject(rowIdx);
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
            }
        };
    }


}
