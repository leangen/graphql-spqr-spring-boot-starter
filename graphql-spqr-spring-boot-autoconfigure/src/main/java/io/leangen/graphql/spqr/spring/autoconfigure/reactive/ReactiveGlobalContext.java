package io.leangen.graphql.spqr.spring.autoconfigure.reactive;

import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReactiveGlobalContext {

    private final ServerWebExchange exchange;
    private final Map<String, Object> extensions;

    public ReactiveGlobalContext(ServerWebExchange exchange) {
        this.exchange = exchange;
        this.extensions = new ConcurrentHashMap<>();
    }

    public ServerWebExchange getExchange() {
        return exchange;
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
