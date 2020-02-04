package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionInput;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.ContextFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.ContextFactoryParams;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

@FunctionalInterface
public interface GraphQLExecutor<R> {

    Object execute(GraphQL graphQL, GraphQLRequest graphQLRequest, R request);

    default ExecutionInput buildInput(GraphQLRequest graphQLRequest, R request, ContextFactory<R> contextFactory,
                                      DataLoaderRegistryFactory loaderFactory) {
        return buildInput(graphQLRequest, request, null, contextFactory, loaderFactory);
    }

    default ExecutionInput buildInput(GraphQLRequest graphQLRequest, R request, Object env, ContextFactory<R> contextFactory,
                                         DataLoaderRegistryFactory loaderFactory) {
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(graphQLRequest.getVariables())
                .context(contextFactory.createGlobalContext(ContextFactoryParams.<R>builder()
                        .withGraphQLRequest(graphQLRequest)
                        .withNativeRequest(request)
                        .withEnvironment(env)
                        .build()));
        if (loaderFactory != null) {
            inputBuilder.dataLoaderRegistry(loaderFactory.createDataLoaderRegistry());
        }
        return inputBuilder.build();
    }
}
