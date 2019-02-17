package io.leangen.graphql.spqr.spring.web.reactive;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContextFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContextFactoryParams;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GraphQLReactiveExecutor implements GraphQLExecutor<ServerWebExchange, CompletableFuture<Map<String, Object>>> {

    private final ReactiveGlobalContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public GraphQLReactiveExecutor(ReactiveGlobalContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public CompletableFuture<Map<String, Object>> execute(GraphQL graphQL, GraphQLRequest graphQLRequest, ServerWebExchange request) {
        return graphQL.executeAsync(input(graphQLRequest, request)).thenApply(ExecutionResult::toSpecification);
    }

    protected ExecutionInput input(GraphQLRequest request, ServerWebExchange exchange) {
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .context(contextFactory.createGlobalContext(ReactiveGlobalContextFactoryParams.builder()
                        .withGraphQLRequest(request)
                        .withWebExchange(exchange)
                        .build()));
        if (dataLoaderRegistryFactory != null) {
            inputBuilder.dataLoaderRegistry(dataLoaderRegistryFactory.createDataLoaderRegistry());
        }
        return inputBuilder.build();
    }
}
