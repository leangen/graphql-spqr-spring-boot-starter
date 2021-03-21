package io.leangen.graphql.spqr.spring.web.mvc;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.MvcContextFactory;
import io.leangen.graphql.spqr.spring.web.HttpExecutor;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import org.reactivestreams.Publisher;
import org.springframework.web.context.request.NativeWebRequest;
import reactor.core.publisher.Mono;

public class DefaultGraphQLExecutor extends HttpExecutor<NativeWebRequest> implements GraphQLMvcExecutor {

    public DefaultGraphQLExecutor(MvcContextFactory contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        super(contextFactory, dataLoaderRegistryFactory);
    }

    public GraphQLMvcExecutor blocking() {
        return new Blocking();
    }

    private class Blocking implements GraphQLMvcExecutor {
        @Override
        public Object execute(GraphQL graphQL, ExecutorParams<NativeWebRequest> params) {
            return Mono.from((Publisher<?>) DefaultGraphQLExecutor.this.execute(graphQL, params)).block();
        }
    }
}
