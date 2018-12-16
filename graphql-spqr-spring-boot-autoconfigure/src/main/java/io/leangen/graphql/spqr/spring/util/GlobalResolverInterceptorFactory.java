package io.leangen.graphql.spqr.spring.util;

import io.leangen.graphql.execution.ResolverInterceptor;
import io.leangen.graphql.execution.ResolverInterceptorFactory;
import io.leangen.graphql.execution.ResolverInterceptorFactoryParams;

import java.util.List;

public class GlobalResolverInterceptorFactory implements ResolverInterceptorFactory {

    private final List<ResolverInterceptor> interceptors;

    public GlobalResolverInterceptorFactory(List<ResolverInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public List<ResolverInterceptor> getInterceptors(ResolverInterceptorFactoryParams params) {
        return interceptors;
    }
}
