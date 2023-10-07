package io.qpointz.rapids.calcite.schemas.blob;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BlobPathTest {

    @Test
    void partialURI() {
        var uri = URI.create("agh://sss/sdds/dsdsds");
        //log.info(uri.toString());
    }

}