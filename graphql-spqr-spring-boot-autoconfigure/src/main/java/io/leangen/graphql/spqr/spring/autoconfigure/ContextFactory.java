package io.leangen.graphql.spqr.spring.autoconfigure;

public interface ContextFactory<R> {

    Object createGlobalContext(ContextFactoryParams<R> params);
}
