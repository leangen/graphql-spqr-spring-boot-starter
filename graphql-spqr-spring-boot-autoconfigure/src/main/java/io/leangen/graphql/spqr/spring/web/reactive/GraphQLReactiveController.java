package io.leangen.graphql.spqr.spring.web.reactive;

import graphql.ExecutionInput;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContextFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContextFactoryParams;
import io.leangen.graphql.spqr.spring.web.AbstractController;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin
public class GraphQLReactiveController extends AbstractController<ServerHttpRequest> {
    private final ReactiveGlobalContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;
    @Value("${graphql.spqr.http.endpoint:/graphql}")
    private String t;


    @Autowired
    public GraphQLReactiveController(GraphQL graphQL, ReactiveGlobalContextFactory contextFactory,
                                     DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        super(graphQL);
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }


    protected ExecutionInput input(GraphQLRequest request, ServerHttpRequest raw) {
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .context(contextFactory.createGlobalContext(ReactiveGlobalContextFactoryParams.builder()
                        .withGraphQLRequest(request)
                        .withHttpRequest(raw)
                        .build()));
        if (dataLoaderRegistryFactory != null) {
            inputBuilder.dataLoaderRegistry(dataLoaderRegistryFactory.createDataLoaderRegistry());
        }
        return inputBuilder.build();
    }
}
