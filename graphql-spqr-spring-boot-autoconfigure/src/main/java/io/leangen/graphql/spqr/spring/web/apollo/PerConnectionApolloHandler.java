package io.leangen.graphql.spqr.spring.web.apollo;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.servlet.websocket.GraphQLWebSocketExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerConnectionApolloHandler implements WebSocketHandler, SubProtocolCapable {

    private final GraphQL graphQL;
    private final GraphQLWebSocketExecutor executor;
    private final TaskScheduler taskScheduler;
    private final int keepAliveInterval;
    private final Map<WebSocketSession, ApolloProtocolHandler> handlers;

    private static final List<String> GRAPHQL_WS = Collections.singletonList("graphql-ws");

    public PerConnectionApolloHandler(GraphQL graphQL, GraphQLWebSocketExecutor executor,
                                      TaskScheduler taskScheduler, int keepAliveInterval) {
        this.graphQL = graphQL;
        this.executor = executor;
        this.taskScheduler = taskScheduler;
        this.keepAliveInterval = keepAliveInterval;
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ApolloProtocolHandler handler = new ApolloProtocolHandler(graphQL, executor, taskScheduler, keepAliveInterval);
        this.handlers.put(session, handler);
        handler.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        getHandler(session).handleMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        getHandler(session).handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        try {
            getHandler(session).afterConnectionClosed(session, closeStatus);
        }
        finally {
            this.handlers.remove(session);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public List<String> getSubProtocols() {
        return GRAPHQL_WS;
    }

    @PreDestroy
    public void cancelAll() {
        this.handlers.forEach((session, handler) -> {
            try {
                session.close(CloseStatus.GOING_AWAY);
            } catch (IOException ignored) {
                /*no-op*/
            }
            handler.cancelAll();
        });
    }

    private WebSocketHandler getHandler(WebSocketSession session) {
        WebSocketHandler handler = this.handlers.get(session);
        if (handler == null) {
            throw new IllegalStateException("WebSocketHandler not found for " + session);
        }
        return handler;
    }
}
