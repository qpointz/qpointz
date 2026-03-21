package io.qpointz.mill.security.authorization.policy.io;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.CallNode;
import io.qpointz.mill.security.authorization.policy.expression.FieldRefNode;
import io.qpointz.mill.security.authorization.policy.expression.LiteralNode;
import io.qpointz.mill.security.authorization.policy.model.ActionType;
import io.qpointz.mill.security.authorization.policy.model.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyImportExportTest {

    private Collection<Policy> samplePolicies() {
        return List.of(
                Policy.builder().name("analysts").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("SALES", "*"))
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("HR", "SALARY"))
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("SCHEMA", "TABLE"))
                                .rawExpression("department = 'analytics'")
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("SCHEMA", "TABLE"))
                                .expression(CallNode.eq(
                                        FieldRefNode.builder().fieldName("department").build(),
                                        LiteralNode.builder().value("analytics").build()
                                ))
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.COLUMN_ACCESS)
                                .table(List.of("SCHEMA", "TABLE"))
                                .columns(List.of("col1", "col2"))
                                .columnsMode(ColumnsMode.INCLUDE)
                                .build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.COLUMN_ACCESS)
                                .table(List.of("SALES", "CLIENT"))
                                .columns(List.of("pii_*"))
                                .columnsMode(ColumnsMode.EXCLUDE)
                                .build()
                )).build(),
                Policy.builder().name("anonymous").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("SCHEMA", "SENSITIVE_TABLE"))
                                .build()
                )).build()
        );
    }

    @Test
    void jsonRoundtrip() throws IOException {
        var exporter = new JsonPolicyExporter();
        var importer = new JsonPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(samplePolicies(), out);

        var imported = importer.importPolicies(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(2, imported.size());
        var policiesList = List.copyOf(imported);
        assertEquals("analysts", policiesList.get(0).getName());
        assertEquals(6, policiesList.get(0).getActions().size());
        assertEquals("anonymous", policiesList.get(1).getName());
    }

    @Test
    void yamlRoundtrip() throws IOException {
        var exporter = new YamlPolicyExporter();
        var importer = new YamlPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(samplePolicies(), out);

        var imported = importer.importPolicies(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(2, imported.size());
        var policiesList = List.copyOf(imported);
        assertEquals("analysts", policiesList.get(0).getName());
        assertEquals(6, policiesList.get(0).getActions().size());
    }

    @Test
    void crossFormat_yamlToJson() throws IOException {
        var yamlExporter = new YamlPolicyExporter();
        var yamlImporter = new YamlPolicyImporter();
        var jsonExporter = new JsonPolicyExporter();
        var jsonImporter = new JsonPolicyImporter();

        var yamlOut = new ByteArrayOutputStream();
        yamlExporter.export(samplePolicies(), yamlOut);

        var fromYaml = yamlImporter.importPolicies(new ByteArrayInputStream(yamlOut.toByteArray()));

        var jsonOut = new ByteArrayOutputStream();
        jsonExporter.export(fromYaml, jsonOut);

        var fromJson = jsonImporter.importPolicies(new ByteArrayInputStream(jsonOut.toByteArray()));

        assertEquals(List.copyOf(fromYaml), List.copyOf(fromJson));
    }

    @Test
    void jsonRoundtrip_preservesStructuredExpression() throws IOException {
        var policies = List.of(
                Policy.builder().name("test").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .expression(CallNode.and(
                                        CallNode.eq(
                                                FieldRefNode.builder().fieldName("a").build(),
                                                LiteralNode.builder().value(1).build()
                                        ),
                                        CallNode.gt(
                                                FieldRefNode.builder().fieldName("b").build(),
                                                LiteralNode.builder().value(100).build()
                                        )
                                ))
                                .build()
                )).build()
        );

        var exporter = new JsonPolicyExporter();
        var importer = new JsonPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(policies, out);

        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(out.toByteArray())));
        var action = imported.get(0).getActions().get(0);
        assertNotNull(action.getExpression());
        assertInstanceOf(CallNode.class, action.getExpression());
        assertEquals("and", ((CallNode) action.getExpression()).getOperator());
    }

    @Test
    void jsonRoundtrip_preservesWildcardTableRefs() throws IOException {
        var policies = List.of(
                Policy.builder().name("test").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW)
                                .type(ActionType.TABLE_ACCESS)
                                .table(List.of("SALES", "*"))
                                .build()
                )).build()
        );

        var exporter = new JsonPolicyExporter();
        var importer = new JsonPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(policies, out);

        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(List.of("SALES", "*"), imported.get(0).getActions().get(0).getTable());
    }

    @Test
    void jsonExport_emptyCollection() throws IOException {
        var exporter = new JsonPolicyExporter();
        var importer = new JsonPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(List.of(), out);

        var imported = importer.importPolicies(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(imported.isEmpty());
    }

    @Test
    void yamlExport_emptyCollection() throws IOException {
        var exporter = new YamlPolicyExporter();
        var importer = new YamlPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(List.of(), out);

        var imported = importer.importPolicies(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(imported.isEmpty());
    }

    @Test
    void jsonImport_allActionTypes() throws IOException {
        var policies = List.of(
                Policy.builder().name("full").actions(List.of(
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.TABLE_ACCESS)
                                .table(List.of("S", "T")).build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.ALLOW).type(ActionType.ROW_FILTER)
                                .table(List.of("S", "T"))
                                .rawExpression("x = 1").build(),
                        PolicyActionEntry.builder()
                                .verb(ActionVerb.DENY).type(ActionType.COLUMN_ACCESS)
                                .table(List.of("S", "T"))
                                .columns(List.of("secret")).columnsMode(ColumnsMode.EXCLUDE).build()
                )).build()
        );

        var exporter = new JsonPolicyExporter();
        var importer = new JsonPolicyImporter();

        var out = new ByteArrayOutputStream();
        exporter.export(policies, out);

        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(3, imported.get(0).getActions().size());
        assertEquals(ActionType.TABLE_ACCESS, imported.get(0).getActions().get(0).getType());
        assertEquals(ActionType.ROW_FILTER, imported.get(0).getActions().get(1).getType());
        assertEquals(ActionType.COLUMN_ACCESS, imported.get(0).getActions().get(2).getType());
    }

    @Test
    void yamlImport_structuredExpression_usesRefPrefixAndLiterals() throws IOException {
        var yaml = """
                policies:
                  - name: analysts
                    actions:
                      - verb: ALLOW
                        type: row-filter
                        table: [SALES, TRANSACTIONS]
                        expression:
                          and:
                            - eq: ["#ref.department", "analytics"]
                            - gt: ["#ref.amount", 1000]
                """;

        var importer = new YamlPolicyImporter();
        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(yaml.getBytes())));
        var action = imported.get(0).getActions().get(0);
        assertNotNull(action.getExpression());
        assertNull(action.getRawExpression());
        assertInstanceOf(CallNode.class, action.getExpression());
        var andNode = (CallNode) action.getExpression();
        assertEquals("and", andNode.getOperator());
        var eqNode = (CallNode) andNode.getOperands().get(0);
        assertEquals("eq", eqNode.getOperator());
        assertInstanceOf(FieldRefNode.class, eqNode.getOperands().get(0));
        assertEquals("department", ((FieldRefNode) eqNode.getOperands().get(0)).getFieldName());
        assertInstanceOf(LiteralNode.class, eqNode.getOperands().get(1));
        assertEquals("analytics", ((LiteralNode) eqNode.getOperands().get(1)).getValue());
    }

    @Test
    void yamlImport_expressionString_mapsToRawExpression() throws IOException {
        var yaml = """
                policies:
                  - name: analysts
                    actions:
                      - verb: ALLOW
                        type: row-filter
                        table: [SALES, TRANSACTIONS]
                        expression: "department = 'analytics'"
                """;

        var importer = new YamlPolicyImporter();
        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(yaml.getBytes())));
        var action = imported.get(0).getActions().get(0);
        assertNull(action.getExpression());
        assertEquals("department = 'analytics'", action.getRawExpression());
    }

    @Test
    void yamlImport_structuredExpression_supportsRefAndConstObjects() throws IOException {
        var yaml = """
                policies:
                  - name: analysts
                    actions:
                      - verb: ALLOW
                        type: row-filter
                        table: [SALES, TRANSACTIONS]
                        expression:
                          eq:
                            - ref: department
                            - const: analytics
                """;

        var importer = new YamlPolicyImporter();
        var imported = List.copyOf(importer.importPolicies(new ByteArrayInputStream(yaml.getBytes())));
        var action = imported.get(0).getActions().get(0);
        assertInstanceOf(CallNode.class, action.getExpression());
        var eqNode = (CallNode) action.getExpression();
        assertEquals("eq", eqNode.getOperator());
        assertInstanceOf(FieldRefNode.class, eqNode.getOperands().get(0));
        assertEquals("department", ((FieldRefNode) eqNode.getOperands().get(0)).getFieldName());
        assertInstanceOf(LiteralNode.class, eqNode.getOperands().get(1));
        assertEquals("analytics", ((LiteralNode) eqNode.getOperands().get(1)).getValue());
    }
}
