package io.leangen.graphql.spqr.spring.web.apollo;

@SuppressWarnings("WeakerAccess")
abstract class PayloadMessage<T> extends ApolloMessage {

    private final T payload;

    PayloadMessage(String id, String type, T payload) {
        super(id, type);
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }
}
