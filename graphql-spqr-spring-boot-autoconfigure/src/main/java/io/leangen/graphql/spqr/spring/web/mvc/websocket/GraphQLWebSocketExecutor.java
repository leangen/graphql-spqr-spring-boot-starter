package io.leangen.graphql.spqr.spring.web.mvc.websocket;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import org.springframework.web.socket.WebSocketSession;

@FunctionalInterface
public interface GraphQLWebSocketExecutor extends GraphQLExecutor<WebSocketSession> {

    @Override
    ExecutionResult execute(GraphQL graphQL, ExecutorParams<WebSocketSession> params);
}
