package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.types.RapidsType;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AvroSchemaConverterTest {

    private Schema byName(String name) {
        var parser = new Schema.Parser();
        var file = "formats/parquet/avro/"+ name+".json";
        var ins = this.getClass().getClassLoader().getResourceAsStream(file);
        try {
            return parser.parse(ins);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private java.util.Map<String, RapidsType> convertByName(String name) {
        var s = this.byName(name);
        var sc =  new AvroSchemaConverter().convert(s);
        return sc;
    }

    @Test
    void readSchema() {
        var s = this.byName("city");
        assertNotNull(s);
    }

    @Test
    void convertSchema(){
        var s = this.byName("city");
        var c = new AvroSchemaConverter();
        var cs = c.convert(s);
        assertNotNull(cs);
    }

    @Test
    void multiunionShouldThrow() {
        assertThrows(RuntimeException.class, ()-> convertByName("multiunion"));
    }

    @Test
    void nonnullableShouldThrow() {
        assertThrows(RuntimeException.class, ()-> convertByName("notnullableunion"));
    }

    @Test
    void unsupportedThrows() {
        assertThrows(RuntimeException.class, ()-> convertByName("unsupported"));
    }

    @Test
    void allValidTypes() {
        var r = convertByName("allTypes");
        assertNotNull(r);
        assertTrue(r.keySet().size()>0);
    }
}