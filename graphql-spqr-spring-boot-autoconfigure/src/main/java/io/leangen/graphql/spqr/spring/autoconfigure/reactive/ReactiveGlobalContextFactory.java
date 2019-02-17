package io.leangen.graphql.spqr.spring.autoconfigure.reactive;

public interface ReactiveGlobalContextFactory {
    Object createGlobalContext(ReactiveGlobalContextFactoryParams params);
}
