package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.types.RapidsType;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void readSchema() {
        var s = this.byName("city");
        assertNotNull(s);
    }

    @Test
    public void convertSchema(){
        var s = this.byName("city");
        var c = new AvroSchemaConverter();
        var cs = c.convert(s);
        assertNotNull(cs);
    }

    @Test
    public void multiunionShouldThrow() {
        assertThrows(RuntimeException.class, ()-> convertByName("multiunion"));
    }

    @Test
    public void nonnullableShouldThrow() {
        assertThrows(RuntimeException.class, ()-> convertByName("notnullableunion"));
    }

    @Test
    public void unsupportedThrows() {
        assertThrows(RuntimeException.class, ()-> convertByName("unsupported"));
    }

    @Test
    public void allTypes() {
        convertByName("allTypes");
    }



}