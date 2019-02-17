package io.leangen.graphql.spqr.spring.autoconfigure.reactive;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.server.ServerWebExchange;

public class ReactiveGlobalContextFactoryParams {

    private final GraphQLRequest graphQLRequest;
    private final ServerWebExchange exchange;

    private ReactiveGlobalContextFactoryParams(GraphQLRequest graphQLRequest, ServerWebExchange exchange) {
        this.graphQLRequest = graphQLRequest;
        this.exchange = exchange;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public ServerWebExchange getWebExchange() {
        return exchange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLRequest graphQLRequest;
        private ServerWebExchange exchange;

        public Builder withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder withWebExchange(ServerWebExchange exchange) {
            this.exchange = exchange;
            return this;
        }

        public ReactiveGlobalContextFactoryParams build() {
            return new ReactiveGlobalContextFactoryParams(graphQLRequest, exchange);
        }
    }
}
