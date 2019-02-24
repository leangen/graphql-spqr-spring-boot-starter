package io.leangen.graphql.spqr.spring.autoconfigure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGlobalContext<R> {

    private final R nativeRequest;
    private final Map<String, Object> extensions;

    public DefaultGlobalContext(R request) {
        this.nativeRequest = request;
        this.extensions = new ConcurrentHashMap<>();
    }

    public R getNativeRequest() {
        return nativeRequest;
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
