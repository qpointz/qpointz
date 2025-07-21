package io.qpointz.flow.formats.excell;

import lombok.val;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class SheetReaderTest {

    @Test
    void trivia() throws IOException {
        val is = SheetReaderTest.class.getClassLoader().getResourceAsStream("flow-excel-test/TestData.xlsx");
        val wb = WorkbookFactory.create(is);
        val sheet = wb.getSheetAt(0);
        val sr = new SheetReader(sheet);
        val records = StreamSupport.stream(sr.spliterator(), false)
                .toList();
        assertTrue(!records.isEmpty());
    }

}