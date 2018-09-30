package io.leangen.graphql.spqr.spring.web.apollo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_INIT;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_CONNECTION_TERMINATE;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_START;
import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_STOP;

class ApolloProtocolHandler extends TextWebSocketHandler {

    private final GraphQL graphQL;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final AtomicReference<WebSocketSession> session = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(ApolloProtocolHandler.class);

    public ApolloProtocolHandler(GraphQL graphQL) {
        this.graphQL = graphQL;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session.set(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelAll();
        this.session.set(null);
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
                    session.sendMessage(ApolloMessage.keepAlive());
                    break;
                case GQL_START:
                    GraphQLRequest request = ((StartMessage) apolloMessage).getPayload();
                    ExecutionResult result = graphQL.execute(ExecutionInput.newExecutionInput()
                            .query(request.getQuery())
                            .operationName(request.getOperationName())
                            .variables(request.getVariables()));
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
            throw new UncheckedIOException(e);
//            fatalError(session, e);
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

    @PreDestroy
    public void cancelAll() {
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

    /*@Scheduled(fixedDelay = -1)
    public void keepAlive() {
        try {
            WebSocketSession s = session.get();
            if (s != null && s.isOpen()) {
                s.sendMessage(ApolloMessage.keepAlive());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
