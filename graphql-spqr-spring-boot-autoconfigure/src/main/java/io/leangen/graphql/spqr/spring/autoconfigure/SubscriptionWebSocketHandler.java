package io.leangen.graphql.spqr.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class SubscriptionWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private GraphQLSchema graphQLSchema;

    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        if (subscriptionRef.get() != null) subscriptionRef.get().cancel();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        GraphQL exe = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionResult res = exe.execute(message.getPayload());
        Publisher<ExecutionResult> stream = res.getData();

        stream.subscribe(new Subscriber<ExecutionResult>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscriptionRef.set(subscription);
                if (subscriptionRef.get() != null) subscriptionRef.get().request(1);
            }

            @Override
            public void onNext(ExecutionResult result) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(result.toSpecification())));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                if (subscriptionRef.get() != null) subscriptionRef.get().request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onComplete() {
                try {
                    session.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }
}
