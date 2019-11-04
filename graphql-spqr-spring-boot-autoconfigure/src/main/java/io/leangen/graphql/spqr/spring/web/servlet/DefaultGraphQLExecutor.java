package io.leangen.graphql.spqr.spring.web.servlet;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.cachecontrol.CacheControl;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ServletContextFactory;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;

public class DefaultGraphQLExecutor implements GraphQLServletExecutor {

    private final ServletContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public DefaultGraphQLExecutor(ServletContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public Map<String, Object> execute(GraphQL graphQL, GraphQLRequest graphQLRequest, NativeWebRequest nativeRequest) {
        CacheControl cacheControl = CacheControl.newCacheControl();
        ExecutionResult executionResult = graphQL.execute(buildInput(graphQLRequest, nativeRequest, contextFactory, dataLoaderRegistryFactory, cacheControl));
        executionResult = cacheControl.addTo(executionResult);
        return executionResult.toSpecification();
    }
}
