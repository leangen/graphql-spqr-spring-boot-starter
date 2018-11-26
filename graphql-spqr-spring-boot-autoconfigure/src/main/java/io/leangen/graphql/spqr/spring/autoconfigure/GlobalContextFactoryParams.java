package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("WeakerAccess")
public class GlobalContextFactoryParams {

    private final GraphQLRequest graphQLRequest;
    private final HttpServletRequest httpRequest;

    private GlobalContextFactoryParams(GraphQLRequest graphQLRequest, HttpServletRequest httpRequest) {
        this.graphQLRequest = graphQLRequest;
        this.httpRequest = httpRequest;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLRequest graphQLRequest;
        private HttpServletRequest httpRequest;

        public Builder withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder withHttpRequest(HttpServletRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }

        public GlobalContextFactoryParams build() {
            return new GlobalContextFactoryParams(graphQLRequest, httpRequest);
        }
    }
}
