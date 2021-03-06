package io.leangen.graphql.spqr.spring.web.mvc.websocket;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.WebSocketContextFactory;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import org.springframework.web.socket.WebSocketSession;

public class DefaultGraphQLExecutor implements GraphQLWebSocketExecutor {

    private final WebSocketContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public DefaultGraphQLExecutor(WebSocketContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public ExecutionResult execute(GraphQL graphQL, ExecutorParams<WebSocketSession> params) {
        return graphQL.execute(buildInput(params.graphQLRequest, params.request, contextFactory, dataLoaderRegistryFactory));
    }
}
