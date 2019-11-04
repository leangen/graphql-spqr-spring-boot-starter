package io.leangen.graphql.spqr.spring.web.reactive;

import graphql.GraphQL;
import graphql.cachecontrol.CacheControl;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ReactiveContextFactory;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultGraphQLExecutor implements GraphQLReactiveExecutor {

    private final ReactiveContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public DefaultGraphQLExecutor(ReactiveContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public CompletableFuture<Map<String, Object>> execute(GraphQL graphQL, GraphQLRequest graphQLRequest, ServerWebExchange request) {
        CacheControl cacheControl = CacheControl.newCacheControl();
        return graphQL.executeAsync(buildInput(graphQLRequest, request, contextFactory, dataLoaderRegistryFactory, cacheControl))
                .thenApply((executionResult1) -> cacheControl.addTo(executionResult1).toSpecification());
    }
}
