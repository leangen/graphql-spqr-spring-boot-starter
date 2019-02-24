package io.leangen.graphql.spqr.spring.web.apollo;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.servlet.websocket.GraphQLWebSocketExecutor;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
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
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
        cancelAll();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ApolloMessage apolloMessage;
            try {
                apolloMessage = ApolloMessage.from(message);
            } catch (IOException e) {
                session.sendMessage(ApolloMessage.connectionError());
                return;
            }
            switch (apolloMessage.getType()) {
                case GQL_CONNECTION_INIT:
                    session.sendMessage(ApolloMessage.connectionAck());
                    if (taskScheduler != null) {
                        session.sendMessage(ApolloMessage.keepAlive());
                    }
                    break;
                case GQL_START:
                    GraphQLRequest request = ((StartMessage) apolloMessage).getPayload();
                    ExecutionResult result = executor.execute(graphQL, request, session);
                    if (result.getData() instanceof Publisher) {
                        handleSubscription(apolloMessage.getId(), result, session);
                    } else {
                        handleQueryOrMutation(apolloMessage.getId(), result, session);
                    }
                    break;
                case GQL_STOP:
                    Subscription toStop = subscriptions.get(apolloMessage.getId());
                    if (toStop != null) {
                        toStop.cancel();
                        subscriptions.remove(apolloMessage.getId(), toStop);
                    }
                    break;
                case GQL_CONNECTION_TERMINATE:
                    session.close();
                    cancelAll();
                    break;
            }
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void handleQueryOrMutation(String id, ExecutionResult result, WebSocketSession session) {
        try {
            session.sendMessage(ApolloMessage.data(id, result));
            session.sendMessage(ApolloMessage.complete(id));
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void handleSubscription(String id, ExecutionResult result, WebSocketSession session) {
        Publisher<ExecutionResult> stream = result.getData();
        Subscriber<ExecutionResult> subscriber = new Subscriber<ExecutionResult>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscriptions.put(id, subscription);
                request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                try {
                    if (executionResult.getErrors().isEmpty()) {
                        session.sendMessage(ApolloMessage.data(id, executionResult));
                    } else {
                        session.sendMessage(ApolloMessage.error(id, executionResult.getErrors()));
                    }
                } catch (IOException e) {
                    fatalError(session, e);
                }
                request(1);
            }

            @Override
            public void onError(Throwable t) {
                try {
                    session.sendMessage(ApolloMessage.error(id, t));
                } catch (IOException e) {
                    fatalError(session, e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    session.sendMessage(ApolloMessage.complete(id));
                } catch (IOException e) {
                    fatalError(session, e);
                }
            }

            private void request(int n) {
                Subscription subscription = this.subscription;
                if (subscription != null) {
                    subscription.request(n);
                }
            }
        };
        stream.subscribe(subscriber);
    }

    void cancelAll() {
        synchronized (subscriptions) {
            subscriptions.values().forEach(Subscription::cancel);
            subscriptions.clear();
        }
    }

    private void fatalError(WebSocketSession session, Exception exception) {
        try {
            session.close(CloseStatus.SESSION_NOT_RELIABLE);
        } catch (Exception ignored) {/*no-op*/}
        cancelAll();
        log.warn(String.format("WebSocket session %s (%s) closed due to an exception", session.getId(), session.getRemoteAddress()), exception);
    }

    private Runnable keepAliveTask(WebSocketSession session) {
        return () -> {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(ApolloMessage.keepAlive());
                }
            } catch (Exception exception) {
                try {
                    session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (Exception ignored) {/*no-op*/}
            }
        };
    }
}
