package io.qpointz.mill.services.jdbc;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.calcite.providers.PlanConverter;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import io.qpointz.mill.services.jdbc.providers.JdbcCalciteContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcConnectionProvider;
import io.qpointz.mill.services.jdbc.providers.JdbcContextFactory;
import io.qpointz.mill.services.jdbc.providers.JdbcExecutionProvider;
import io.qpointz.mill.services.jdbc.providers.impl.JdbcConnectionCustomizerImpl;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = {
        BackendConfiguration.class,
        JdbcCalciteConfiguration.class,
        DefaultServiceConfiguration.class,
        JdbcConnectionProviderTestIT.TestConfig.class
})
@ActiveProfiles("test-moneta-it")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
class JdbcConnectionProviderTestIT {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public JdbcConnectionProvider testJdbcConnectionProvider() {
            // Create a spy that delegates to the real implementation
            return spy(new JdbcConnectionCustomizerImpl());
        }
    }

    @Autowired
    private JdbcContextFactory jdbcContextFactory;

    @Autowired
    private JdbcConnectionProvider jdbcConnectionProvider;

    @Autowired
    private BackendConfiguration backendConfiguration;

    @Autowired
    private JdbcCalciteConfiguration calciteConfiguration;

    @Mock
    private PlanConverter planConverter;

    @BeforeEach
    void setUp() {
        reset(jdbcConnectionProvider);
    }

    @Test
    void createConnection_called_whenExecutingSql() throws SQLException {
        // Arrange
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `clients` LIMIT 1", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        
        // Create a real connection to return from the spy
        val realConnection = DriverManager.getConnection(
                "jdbc:h2:mem:moneta-slim;INIT=RUNSCRIPT FROM '../../test/datasets/moneta/moneta-slim.sql'");
        
        doReturn(realConnection).when(jdbcConnectionProvider)
                .createConnection(anyString(), anyString(), anyString(), anyString());
        
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        
        // Act
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        
        // Assert
        verify(jdbcConnectionProvider, atLeastOnce())
                .createConnection(anyString(), anyString(), anyString(), anyString());
        
        assertTrue(r.hasNext(), "Should have results");
        val b = r.next();
        assertTrue(b.getVectorSize() > 0, "Should have at least one row");
        
        realConnection.close();
    }


    @Test
    void customizeSchemaOperand_receivesCorrectParameters() throws Exception {
        // Arrange
        reset(jdbcConnectionProvider);
        
        val capturedOperand = new HashMap<String, Object>();
        
        doAnswer(invocation -> {
            val targetName = invocation.getArgument(0, String.class);
            val catalog = invocation.getArgument(1, Optional.class);
            val schema = invocation.getArgument(2, Optional.class);
            val operand = invocation.getArgument(3, Map.class);
            
            // Capture the operand
            capturedOperand.putAll(operand);
            
            // Verify parameters
            assertNotNull(targetName, "targetName should not be null");
            assertNotNull(operand, "operand should not be null");
            assertTrue(operand.containsKey("jdbcUrl"), "operand should contain jdbcUrl");
            assertTrue(operand.containsKey("jdbcDriver"), "operand should contain jdbcDriver");
            
            // Delegate to real implementation
            val realImpl = new JdbcConnectionCustomizerImpl();
            return realImpl.customizeSchemaOperand(targetName, catalog, schema, operand);
        }).when(jdbcConnectionProvider).customizeSchemaOperand(anyString(), any(), any(), any());
        
        doAnswer(invocation -> {
            val driver = invocation.getArgument(0, String.class);
            val url = invocation.getArgument(1, String.class);
            val user = invocation.getArgument(2, String.class);
            val password = invocation.getArgument(3, String.class);
            
            val realImpl = new JdbcConnectionCustomizerImpl();
            return realImpl.createConnection(driver, url, user, password);
        }).when(jdbcConnectionProvider).createConnection(anyString(), anyString(), anyString(), anyString());
        
        // Act - Trigger schema creation
        val props = new Properties();
        props.putAll(backendConfiguration.getConnection());
        val calciteContext = new JdbcCalciteContextFactory(props, calciteConfiguration,
                                Optional.of("ts"), jdbcConnectionProvider);
        val subSchemas = calciteContext
                .createContext()
                .getCalciteRootSchema()
                .getSubSchemaMap();
        assertTrue(!subSchemas.isEmpty());

        
        // Assert
        verify(jdbcConnectionProvider, atLeastOnce())
                .customizeSchemaOperand(anyString(), any(Optional.class), any(Optional.class), any(Map.class));
        
        assertFalse(capturedOperand.isEmpty(), "Operand should be captured");
        assertTrue(capturedOperand.containsKey("jdbcUrl"), "Operand should contain jdbcUrl");
        assertTrue(capturedOperand.containsKey("jdbcDriver"), "Operand should contain jdbcDriver");
    }

    @Test
    void createConnection_receivesCorrectParameters() throws SQLException {
        // Arrange
        reset(jdbcConnectionProvider);
        
        val capturedDriver = new String[1];
        val capturedUrl = new String[1];
        
        doAnswer(invocation -> {
            val driver = invocation.getArgument(0, String.class);
            val url = invocation.getArgument(1, String.class);
            val user = invocation.getArgument(2, String.class);
            val password = invocation.getArgument(3, String.class);
            
            // Capture parameters
            capturedDriver[0] = driver;
            capturedUrl[0] = url;
            
            // Verify parameters
            assertNotNull(driver, "driver should not be null");
            assertNotNull(url, "url should not be null");
            assertTrue(url.contains("jdbc:h2"), "url should be H2 JDBC URL");
            
            // Delegate to real implementation
            val realImpl = new JdbcConnectionCustomizerImpl();
            return realImpl.createConnection(driver, url, user, password);
        }).when(jdbcConnectionProvider).createConnection(anyString(), anyString(), anyString(), anyString());
        
        val sql = new PlanConverter.ConvertedPlanSql("SELECT * FROM `clients` LIMIT 1", List.of());
        when(planConverter.toSql(any(Plan.class))).thenReturn(sql);
        val ep = new JdbcExecutionProvider(planConverter, jdbcContextFactory);
        
        // Act
        val r = ep.execute(ImmutablePlan.builder().build(),
                QueryExecutionConfig.newBuilder().setFetchSize(10).build());
        
        // Assert
        verify(jdbcConnectionProvider, atLeastOnce())
                .createConnection(anyString(), anyString(), anyString(), anyString());
        
        assertNotNull(capturedDriver[0], "Driver should be captured");
        assertNotNull(capturedUrl[0], "URL should be captured");
        assertEquals("org.h2.Driver", capturedDriver[0], "Driver should be H2");
        assertTrue(capturedUrl[0].contains("moneta-slim"), "URL should contain moneta-slim");
        
        assertTrue(r.hasNext(), "Should have results");
    }
}

