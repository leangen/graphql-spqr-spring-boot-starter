package io.leangen.graphql.spqr.spring.autoconfigure.reactive;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReactiveGlobalContext {

    private final ServerHttpRequest servletRequest;
    private final Map<String, Object> extensions;

    public ReactiveGlobalContext(ServerHttpRequest servletRequest) {
        this.servletRequest = servletRequest;
        this.extensions = new ConcurrentHashMap<>();
    }

    public ServerHttpRequest getServletRequest() {
        return servletRequest;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key) {
        return (T) extensions.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T setExtension(String key, T value) {
        return (T) extensions.put(key, value);
    }
}
