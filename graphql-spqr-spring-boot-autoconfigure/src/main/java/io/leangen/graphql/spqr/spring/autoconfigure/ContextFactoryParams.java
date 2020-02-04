package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

@SuppressWarnings("WeakerAccess")
public class ContextFactoryParams<R> {

    private final GraphQLRequest graphQLRequest;
    private final R nativeRequest;
    private final Object environment;

    private ContextFactoryParams(GraphQLRequest graphQLRequest, R nativeRequest, Object environment) {
        this.graphQLRequest = graphQLRequest;
        this.nativeRequest = nativeRequest;
        this.environment = environment;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public R getNativeRequest() {
        return nativeRequest;
    }

    public Object getEnvironment() {
        return environment;
    }

    public static <R> Builder<R> builder() {
        return new Builder<>();
    }

    public static class Builder<R> {
        private GraphQLRequest graphQLRequest;
        private R nativeRequest;
        private Object environment;

        public Builder<R> withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder<R> withNativeRequest(R nativeRequest) {
            this.nativeRequest = nativeRequest;
            return this;
        }

        public Builder<R> withEnvironment(Object environment) {
            this.environment = environment;
            return this;
        }

        public ContextFactoryParams<R> build() {
            return new ContextFactoryParams<>(graphQLRequest, nativeRequest, environment);
        }
    }
}
