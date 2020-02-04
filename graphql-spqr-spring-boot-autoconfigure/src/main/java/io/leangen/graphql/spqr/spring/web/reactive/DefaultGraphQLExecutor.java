package io.leangen.graphql.spqr.spring.web.reactive;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ReactiveContextFactory;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class DefaultGraphQLExecutor implements GraphQLReactiveExecutor {

    private final ReactiveContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public DefaultGraphQLExecutor(ReactiveContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public Mono<Map<String, Object>> execute(GraphQL graphQL, GraphQLRequest graphQLRequest, ServerWebExchange request) {
        return Mono.subscriberContext().flatMap(ctx -> Mono.fromFuture(
                graphQL.executeAsync(buildInput(graphQLRequest, request, ctx, contextFactory, dataLoaderRegistryFactory))
                        .thenApply(ExecutionResult::toSpecification))
        );
    }
}
