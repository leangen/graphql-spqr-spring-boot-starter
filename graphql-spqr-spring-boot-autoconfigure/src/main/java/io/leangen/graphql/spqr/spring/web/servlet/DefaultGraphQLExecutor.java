package io.leangen.graphql.spqr.spring.web.servlet;

import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ServletContextFactory;
import io.leangen.graphql.spqr.spring.web.HttpExecutor;
import org.springframework.web.context.request.NativeWebRequest;

public class DefaultGraphQLExecutor extends HttpExecutor<NativeWebRequest> implements GraphQLServletExecutor {

    public DefaultGraphQLExecutor(ServletContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        super(contextFactory, dataLoaderRegistryFactory);
    }
}
