package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

@SuppressWarnings("WeakerAccess")
public class ContextFactoryParams<R> {

    private final GraphQLRequest graphQLRequest;
    private final R nativeRequest;

    private ContextFactoryParams(GraphQLRequest graphQLRequest, R nativeRequest) {
        this.graphQLRequest = graphQLRequest;
        this.nativeRequest = nativeRequest;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public R getNativeRequest() {
        return nativeRequest;
    }

    public static <R> Builder<R> builder() {
        return new Builder<>();
    }

    public static class Builder<R> {
        private GraphQLRequest graphQLRequest;
        private R nativeRequest;

        public Builder<R> withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder<R> withNativeRequest(R nativeRequest) {
            this.nativeRequest = nativeRequest;
            return this;
        }

        public ContextFactoryParams<R> build() {
            return new ContextFactoryParams<>(graphQLRequest, nativeRequest);
        }
    }
}
