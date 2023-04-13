package io.qpointz.rapids.graphql;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.qpointz.rapids.calcite.CalciteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphQlCalciteHandler implements GraphQLHandler {

    private final CalciteHandler calcite;

    public GraphQlCalciteHandler(CalciteHandler calciteHandler) {
        this.calcite = calciteHandler;
        this.init();
    }

    private void init() {
//        log.info("Initing GraphQL handler");
//
//        var builder = new GraphQLObjectType.Builder();
//        builder.name("root");

        //new GraphQLSchema()

  //      final var rootSchema = this.calcite.getRootSchema();
//        buildSchema(builder, rootSchema);
//
    }



}
