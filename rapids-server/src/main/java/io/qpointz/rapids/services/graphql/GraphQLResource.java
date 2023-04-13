package io.qpointz.rapids.services.graphql;

import io.qpointz.rapids.graphql.GraphQLHandler;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/graphql")
@Slf4j
public class GraphQLResource {

    @Inject
    GraphQLHandler graphQl;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public GraphQlResult get(@QueryParam("query") String query,
                             @QueryParam("table") Optional<String> table,
                             @QueryParam("variables") Optional<String> variables,
                             @QueryParam("operationName") Optional<String> operationName
    ) {
        final var queryBuilder = GraphQlQuery.builder();
        queryBuilder.query(query);
        //variables.ifPresent(queryBuilder::variables);
        operationName.ifPresent(queryBuilder::operationName);
        log.info("{}", table.orElse("No table"));
        return exec(queryBuilder.build());
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GraphQlResult post(GraphQlQuery query, @PathParam("path") List<String> segments) {
        log.info("{}", segments);
        return exec(query);
    }

    private GraphQlResult exec(GraphQlQuery query) {
        return new GraphQlResult();
    }


}
