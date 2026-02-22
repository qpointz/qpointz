package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.plan.LogicalFunctionHelper;
import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.Field;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.types.DataTypeToSubstrait;
import io.substrait.dsl.SubstraitBuilder;
import io.substrait.extension.SimpleExtension;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.ImmutableRoot;
import io.substrait.plan.Plan;
import io.substrait.relation.NamedScan;
import io.substrait.relation.Rel;
import io.substrait.type.ImmutableType;
import io.substrait.type.NamedStruct;
import lombok.val;

import java.util.Comparator;
import java.util.List;

public class PlanHelper {

    private final SchemaProvider schemaProvider;
    private final SimpleExtension.ExtensionCollection extensions;
    private final SubstraitBuilder builder;
    private final LogicalFunctionHelper logicalFn;

    public PlanHelper(SchemaProvider schemaProvider, SimpleExtension.ExtensionCollection extensions) {
        this.schemaProvider = schemaProvider;
        this.extensions = extensions;
        this.builder = new SubstraitBuilder(this.extensions);
        this.logicalFn = new LogicalFunctionHelper(this.builder);
    }

    public LogicalFunctionHelper logicalFn() {
        return this.logicalFn;
    }

    public NamedScan createNamedScan(String schemaName, String tableName) {
        val mayBeTable = this.schemaProvider.
                getSchema(schemaName)
                .getTablesList().stream()
                .filter(t-> t.getName().equals(tableName))
                .findAny();

        if (mayBeTable.isEmpty()) {
            throw new RuntimeException("Table " + tableName + " not found");
        }

        val table = mayBeTable.get();

        val names = table.getFieldsList().stream()
                .map(Field::getName)
                .toList();

        val dataTypeMapper = new DataTypeToSubstrait();
        val types = table.getFieldsList().stream()
                .map(k-> dataTypeMapper.toSubstrait(k.getType()))
                .toList();

        val nullabilitySet = DataType.Nullability.NOT_NULL != table.getFieldsList().stream()
                .map(k-> k.getType().getNullability())
                .min(Comparator.comparingInt(DataType.Nullability::getNumber))
                .orElse(DataType.Nullability.NOT_SPECIFIED_NULL);

        val struct = ImmutableType.Struct.builder()
                .addAllFields(types)
                .nullable(nullabilitySet)
                .build();

        NamedStruct namedStruct = NamedStruct.of(names, struct);

        return NamedScan.builder()
                .names(List.of(schemaName, tableName))
                .initialSchema(namedStruct)
                .build();
    }

    public Plan createPlan(Rel namedScan) {
        val root = ImmutableRoot.builder()
                .input(namedScan)
                .build();

        return ImmutablePlan.builder()
                .addRoots(root).build();
    }

    public SubstraitBuilder substraitBuilder() {
        return this.builder;
    }



}
