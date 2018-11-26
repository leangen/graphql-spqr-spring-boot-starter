package io.leangen.graphql.spqr.spring.autoconfigure;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGlobalContext {

    private final HttpServletRequest servletRequest;
    private final Map<String, Object> extensions;

    public DefaultGlobalContext(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
        this.extensions = new ConcurrentHashMap<>();
    }

    public HttpServletRequest getServletRequest() {
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
