package io.leangen.graphql.spqr.spring.web.servlet.websocket;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.cachecontrol.CacheControl;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.WebSocketContextFactory;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.socket.WebSocketSession;

public class DefaultGraphQLExecutor implements GraphQLWebSocketExecutor {

    private final WebSocketContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public DefaultGraphQLExecutor(WebSocketContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public ExecutionResult execute(GraphQL graphQL, GraphQLRequest graphQLRequest, WebSocketSession request) {
        CacheControl cacheControl = CacheControl.newCacheControl();
        ExecutionResult executionResult = graphQL.execute(buildInput(graphQLRequest, request, contextFactory, dataLoaderRegistryFactory, cacheControl));
        executionResult = cacheControl.addTo(executionResult);
        return executionResult;
    }
}
