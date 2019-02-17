package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import org.springframework.web.context.request.NativeWebRequest;

@SuppressWarnings("WeakerAccess")
public class GlobalContextFactoryParams {

    private final GraphQLRequest graphQLRequest;
    private final NativeWebRequest nativeRequest;

    private GlobalContextFactoryParams(GraphQLRequest graphQLRequest, NativeWebRequest nativeRequest) {
        this.graphQLRequest = graphQLRequest;
        this.nativeRequest = nativeRequest;
    }

    public GraphQLRequest getGraphQLRequest() {
        return graphQLRequest;
    }

    public NativeWebRequest getNativeRequest() {
        return nativeRequest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLRequest graphQLRequest;
        private NativeWebRequest nativeRequest;

        public Builder withGraphQLRequest(GraphQLRequest graphQLRequest) {
            this.graphQLRequest = graphQLRequest;
            return this;
        }

        public Builder withNativeRequest(NativeWebRequest nativeRequest) {
            this.nativeRequest = nativeRequest;
            return this;
        }

        public GlobalContextFactoryParams build() {
            return new GlobalContextFactoryParams(graphQLRequest, nativeRequest);
        }
    }
}
