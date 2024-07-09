package io.qpointz.mill.service;

import io.qpointz.mill.proto.DeltaServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        DeltaServiceMetadataTest.class,
        DeltaService.class,
        DeltaServiceBaseTestConfiguration.class})
public abstract class DeltaServiceBaseTest {

    @BeforeEach
    public void resetMocks(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider, @Autowired ExecutionProvider executionProvider) {
        reset(sqlProvider, executionProvider, metadataProvider);
    }

    @Test
    public void testContext(@Autowired SqlProvider sqlProvider, @Autowired MetadataProvider metadataProvider,
                            @Autowired ExecutionProvider executionProvider, @Autowired DeltaService deltaService,
                            @Autowired DeltaServiceGrpc.DeltaServiceBlockingStub blocking) {
        assertNotNull(sqlProvider);
        assertNotNull(metadataProvider);
        assertNotNull(executionProvider);
        assertNotNull(deltaService);
        assertNotNull(blocking);
    }


}
