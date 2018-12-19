package io.leangen.graphql.spqr.spring.autoconfigure.reactive;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class ReactiveGlobalContextFactoryParams {

    private final GraphQLRequest graphQLRequest;
    private final ServerHttpRequest httpRequest;

    private ReactiveGlobalContextFactoryParams(GraphQLRequest graphQLRequest, ServerHttpRequest httpRequest) {
        this.graphQLRequest = graphQLRequest;
        this.httpRequest = httpRequest;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public ServerHttpRequest getHttpRequest() {
        return httpRequest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLRequest graphQLRequest;
        private ServerHttpRequest httpRequest;

        public Builder withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder withHttpRequest(ServerHttpRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }

        public ReactiveGlobalContextFactoryParams build() {
            return new ReactiveGlobalContextFactoryParams(graphQLRequest, httpRequest);
        }
    }
}
