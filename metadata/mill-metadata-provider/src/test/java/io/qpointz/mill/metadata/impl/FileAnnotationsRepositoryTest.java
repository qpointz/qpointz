package io.qpointz.mill.metadata.impl;

import io.qpointz.mill.metadata.impl.file.FileAnnotationsRepository;
import io.qpointz.mill.metadata.impl.file.FileRepository;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


class FileAnnotationsRepositoryTest {

    private static FileAnnotationsRepository defaultRepo() throws IOException {
        val is = new FileInputStream("../../test/datasets/moneta/moneta-meta.yaml");
        val far = new FileAnnotationsRepository(FileRepository.from(is));
        return far;
    }

    @Test
    void trivia() throws IOException {
        final var far = defaultRepo();
        assertNotNull(far);
    }

    @Test
    void schemaDescription() throws IOException {
        val far = defaultRepo();
        //returns existing
        assertTrue(far.getSchemaDescription("moneta").isPresent());
        assertTrue(far.getSchemaDescription("MoNetA").isPresent());

        //empty for faulty
        assertTrue(far.getSchemaDescription("foo").isEmpty());
    }

    @Test
    void tableDescription() throws IOException {
        val far = defaultRepo();
        //returns existing
        assertTrue(far.getTableDescription("moneta", "clients").isPresent());
        assertTrue(far.getTableDescription("MoNetA", "ClIentS").isPresent());

        //empty for faulty
        assertTrue(far.getTableDescription("moneta", "notable").isEmpty());
    }

    @Test
    void attributeDescriptor() throws IOException {
        val far = defaultRepo();
        //returns existing
        assertTrue(far.getAttributeDescription("moneta", "clients", "first_name").isPresent());
        assertTrue(far.getAttributeDescription("MoNetA", "ClIentS","fIrsT_Name").isPresent());

        //empty for faulty
        assertTrue(far.getAttributeDescription("moneta", "clients", "no_attr").isEmpty());
    }


    @Test
    void modelDescription() throws IOException {
        val far = defaultRepo();
        assertTrue(far.getModelDescription().isPresent());
    }

}