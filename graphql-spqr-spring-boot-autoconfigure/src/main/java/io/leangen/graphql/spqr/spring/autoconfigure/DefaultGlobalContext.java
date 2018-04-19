package io.leangen.graphql.spqr.spring.autoconfigure;

import org.dataloader.DataLoaderRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGlobalContext {

    private final HttpServletRequest servletRequest;
    private final DataLoaderRegistry dataLoaders;
    private final Map<String, Object> extensions;

    public DefaultGlobalContext(HttpServletRequest servletRequest, DataLoaderRegistry dataLoaders) {
        this.servletRequest = servletRequest;
        this.dataLoaders = dataLoaders;
        this.extensions = new ConcurrentHashMap<>();
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public DataLoaderRegistry getDataLoaders() {
        return dataLoaders;
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
