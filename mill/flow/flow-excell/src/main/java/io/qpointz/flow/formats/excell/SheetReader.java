package io.qpointz.flow.formats.excell;

import io.qpointz.flow.records.Record;
import io.qpointz.flow.records.RecordAttribute;
import io.qpointz.flow.records.RecordMetaEntry;
import io.qpointz.flow.records.RecordReader;
import lombok.val;
import org.apache.poi.ss.usermodel.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class SheetReader implements RecordReader {

    private final Sheet sheet;

    public SheetReader(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public Iterator<Record> iterator() {
        return StreamSupport.stream(this.sheet.spliterator(), false)
                .map(this::asRecord)
                .iterator();
    }

    private Record asRecord(Row row) {

        val cells = StreamSupport.stream(row.spliterator(), false)
                .toList();
        val rowNum = row.getRowNum();


        return new Record() {
            @Override
            public List<RecordAttribute> attributes() {
                return List.of();
            }

            @Override
            public Object[] values() {
                return new Object[0];
            }

            @Override
            public Collection<RecordMetaEntry> metas() {
                return List.of();
            }
        };
    }
}
