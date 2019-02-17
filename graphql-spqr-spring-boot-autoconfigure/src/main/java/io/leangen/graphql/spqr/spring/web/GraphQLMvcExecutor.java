package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionInput;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.GlobalContextFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.GlobalContextFactoryParams;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;

public class GraphQLMvcExecutor implements GraphQLExecutor<NativeWebRequest, Map<String, Object>> {

    private final GlobalContextFactory contextFactory;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    public GraphQLMvcExecutor(GlobalContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public Map<String, Object> execute(GraphQL graphQL, GraphQLRequest graphQLRequest, NativeWebRequest nativeRequest) {
        return graphQL.execute(input(graphQLRequest, nativeRequest)).toSpecification();
    }

    protected ExecutionInput input(GraphQLRequest graphQLRequest, NativeWebRequest nativeRequest) {
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(graphQLRequest.getVariables())
                .context(contextFactory.createGlobalContext(GlobalContextFactoryParams.builder()
                        .withGraphQLRequest(graphQLRequest)
                        .withNativeRequest(nativeRequest)
                        .build()));
        if (dataLoaderRegistryFactory != null) {
            inputBuilder.dataLoaderRegistry(dataLoaderRegistryFactory.createDataLoaderRegistry());
        }
        return inputBuilder.build();
    }
}
