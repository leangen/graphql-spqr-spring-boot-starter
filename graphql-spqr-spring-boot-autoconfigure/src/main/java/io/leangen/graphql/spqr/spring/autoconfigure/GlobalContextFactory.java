package io.leangen.graphql.spqr.spring.autoconfigure;

public interface GlobalContextFactory {

    Object createGlobalContext(GlobalContextFactoryParams params);
}
