package io.leangen.graphql.spqr.spring.web.apollo;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.mvc.websocket.GraphQLWebSocketExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

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
    private final int sendTimeLimit;
    private final int sendBufferSizeLimit;
    private final Map<WebSocketSession, HandlerProxy> handlers;

    private static final List<String> GRAPHQL_WS = Collections.singletonList("graphql-ws");

    public PerConnectionApolloHandler(GraphQL graphQL, GraphQLWebSocketExecutor executor,
                                      TaskScheduler taskScheduler, int keepAliveInterval,
                                      int sendTimeLimit, int sendBufferSizeLimit) {
        this.graphQL = graphQL;
        this.executor = executor;
        this.taskScheduler = taskScheduler;
        this.keepAliveInterval = keepAliveInterval;
        this.sendTimeLimit = sendTimeLimit;
        this.sendBufferSizeLimit = sendBufferSizeLimit;
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ApolloProtocolHandler handler = new ApolloProtocolHandler(graphQL, executor, taskScheduler, keepAliveInterval);
        HandlerProxy proxy = new HandlerProxy(handler, decorateSession(session));
        this.handlers.put(session, proxy);
        proxy.afterConnectionEstablished();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        getHandler(session).handleMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        getHandler(session).handleTransportError(exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        try {
            getHandler(session).afterConnectionClosed(closeStatus);
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

    protected WebSocketSession decorateSession(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, sendTimeLimit, sendBufferSizeLimit);
    }

    private HandlerProxy getHandler(WebSocketSession session) {
        HandlerProxy handler = this.handlers.get(session);
        if (handler == null) {
            throw new IllegalStateException("WebSocketHandler not found for " + session);
        }
        return handler;
    }

    private static class HandlerProxy {

        private final ApolloProtocolHandler handler;
        private final WebSocketSession session;

        HandlerProxy(ApolloProtocolHandler handler, WebSocketSession session) {
            this.handler = handler;
            this.session = session;
        }

        void afterConnectionEstablished() throws Exception {
            handler.afterConnectionEstablished(session);
        }

        void handleMessage(WebSocketMessage<?> message) throws Exception {
            handler.handleMessage(session, message);
        }

        void handleTransportError(Throwable exception) {
            handler.handleTransportError(session, exception);
        }

        void afterConnectionClosed(CloseStatus closeStatus) {
            handler.afterConnectionClosed(session, closeStatus);
        }

        void cancelAll() {
            handler.cancelAll();
        }
    }
}
