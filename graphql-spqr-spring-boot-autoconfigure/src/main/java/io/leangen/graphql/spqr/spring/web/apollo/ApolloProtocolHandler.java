package io.leangen.graphql.spqr.spring.web.apollo;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.spqr.spring.web.dto.TransportType;
import io.leangen.graphql.spqr.spring.web.mvc.websocket.GraphQLWebSocketExecutor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_INIT;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_TERMINATE;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_START;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_STOP;

class ApolloProtocolHandler extends TextWebSocketHandler {

    private final GraphQL graphQL;
    private final GraphQLWebSocketExecutor executor;
    private final TaskScheduler taskScheduler;
    private final int keepAliveInterval;
    private final Map<String, Disposable> subscriptions = new ConcurrentHashMap<>();
    private final AtomicReference<ScheduledFuture<?>> keepAlive = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(ApolloProtocolHandler.class);

    public ApolloProtocolHandler(GraphQL graphQL, GraphQLWebSocketExecutor executor,
                                 TaskScheduler taskScheduler, int keepAliveInterval) {
        this.graphQL = graphQL;
        this.executor = executor;
        this.taskScheduler = taskScheduler;
        this.keepAliveInterval = keepAliveInterval;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        if (taskScheduler != null) {
            this.keepAlive.compareAndSet(null, taskScheduler.scheduleWithFixedDelay(keepAliveTask(session), Math.max(keepAliveInterval, 1000)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelAll();
        if (taskScheduler != null) {
            this.keepAlive.getAndUpdate(task -> {
                if (task != null) {
                    task.cancel(false);
                }
                return null;
            });
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        fatalError(session, exception);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ApolloMessage apolloMessage;
            try {
                apolloMessage = ApolloMessages.from(message);
            } catch (IOException e) {
                session.sendMessage(ApolloMessages.connectionError());
                return;
            }
            switch (apolloMessage.getType()) {
                case GQL_CONNECTION_INIT:
                    session.sendMessage(ApolloMessages.connectionAck());
                    if (taskScheduler != null) {
                        session.sendMessage(ApolloMessages.keepAlive());
                    }
                    break;
                case GQL_START:
                    GraphQLRequest request = ((StartMessage) apolloMessage).getPayload();
                    ExecutorParams<WebSocketSession> params = new ExecutorParams<>(request, session, TransportType.WEBSOCKET);
                    ExecutionResult result = executor.execute(graphQL, params);
                    if (result.getData() instanceof Publisher) {
                        handleSubscription(apolloMessage.getId(), result, session);
                    } else {
                        handleQueryOrMutation(apolloMessage.getId(), result, session);
                    }
                    break;
                case GQL_STOP:
                    Disposable toStop = subscriptions.get(apolloMessage.getId());
                    if (toStop != null) {
                        toStop.dispose();
                        subscriptions.remove(apolloMessage.getId(), toStop);
                    }
                    break;
                case GQL_CONNECTION_TERMINATE:
                    session.close();
                    cancelAll();
                    break;
            }
        } catch (Exception e) {
            fatalError(session, e);
        }
    }

    private void handleQueryOrMutation(String id, ExecutionResult result, WebSocketSession session) {
        try {
            session.sendMessage(ApolloMessages.data(id, result));
            session.sendMessage(ApolloMessages.complete(id));
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void handleSubscription(String id, ExecutionResult executionResult, WebSocketSession session) {
        Publisher<ExecutionResult> events = executionResult.getData();

        Disposable subscription = Flux.from(events).subscribe(
                result -> onNext(result, id, session),
                error -> onError(error, id, session),
                () -> onComplete(id, session)
        );
        synchronized (subscriptions) {
            subscriptions.put(id, subscription);
        }
    }

    private void onNext(ExecutionResult result, String id, WebSocketSession session) {
        try {
            if (result.getErrors().isEmpty()) {
                session.sendMessage(ApolloMessages.data(id, result));
            } else {
                session.sendMessage(ApolloMessages.error(id, result.getErrors()));
            }
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void onError(Throwable error, String id, WebSocketSession session) {
        try {
            session.sendMessage(ApolloMessages.error(id, error));
            session.sendMessage(ApolloMessages.complete(id));
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void onComplete(String id, WebSocketSession session) {
        try {
            session.sendMessage(ApolloMessages.complete(id));
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    void cancelAll() {
        synchronized (subscriptions) {
            subscriptions.values().forEach(Disposable::dispose);
            subscriptions.clear();
        }
    }

    private void fatalError(WebSocketSession session, Throwable exception) {
        try {
            session.close(exception instanceof IOException ? CloseStatus.SESSION_NOT_RELIABLE : CloseStatus.SERVER_ERROR);
        } catch (Exception suppressed) {
            exception.addSuppressed(suppressed);
        }
        cancelAll();
        log.warn(String.format("WebSocket session %s (%s) closed due to an exception", session.getId(), session.getRemoteAddress()), exception);
    }

    private Runnable keepAliveTask(WebSocketSession session) {
        return () -> {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(ApolloMessages.keepAlive());
                }
            } catch (IOException exception) {
                fatalError(session, exception);
            }
        };
    }
}
