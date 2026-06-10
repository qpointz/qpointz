//package io.qpointz.mill.ai.nlsql.components;
//
//import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Slf4j
//@SpringBootTest(classes = {QueryExecutorTest.class, DefaultServiceConfiguration.class})
//@ComponentScan("io.qpointz")
//@ActiveProfiles("test-moneta-slim")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//class QueryExecutorTest {
//
//
//    @Autowired
//    DataOperationDispatcher dataOperationDispatcher;
//
//
//    @Test
//    void trivial() {
//        val ex = new QueryExecutor(dataOperationDispatcher);
//        val res = ex.submit("SELECT * FROM `MONETA`.`CLIENTS`");
//        assertNotNull(res.continueLink());
//        assertTrue(res.data().size()>0);
//    }
//
//
//}