package io.leangen.graphql.spqr.spring.web.reactive;

import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ReactiveContextFactory;
import io.leangen.graphql.spqr.spring.web.HttpExecutor;
import org.springframework.web.server.ServerWebExchange;

public class DefaultGraphQLExecutor extends HttpExecutor<ServerWebExchange> implements GraphQLReactiveExecutor {

    public DefaultGraphQLExecutor(ReactiveContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        super(contextFactory, dataLoaderRegistryFactory);
    }
}
