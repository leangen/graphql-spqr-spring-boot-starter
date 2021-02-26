package io.leangen.graphql.spqr.spring.web.dto;

public enum TransportType {
    HTTP, HTTP_EVENT_STREAM, WEBSOCKET;

    public boolean isEventStream() {
        return this != HTTP;
    }
}
