package io.leangen.graphql.spqr.spring.web;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import io.leangen.graphql.spqr.spring.autoconfigure.ContextFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.DataLoaderRegistryFactory;
import io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage;
import io.leangen.graphql.spqr.spring.web.apollo.DataMessage;
import io.leangen.graphql.spqr.spring.web.apollo.ErrorMessage;
import io.leangen.graphql.spqr.spring.web.dto.ExecutorParams;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

import static io.leangen.graphql.spqr.spring.web.apollo.ApolloMessage.GQL_COMPLETE;

public abstract class HttpExecutor<R> implements GraphQLExecutor<R> {

    protected final ContextFactory<R> contextFactory;
    protected final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    protected HttpExecutor(ContextFactory<R> contextFactory, DataLoaderRegistryFactory dataLoaderRegistryFactory) {
        this.contextFactory = contextFactory;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory;
    }

    @Override
    public Object execute(GraphQL graphQL, ExecutorParams<R> params) {
        Mono<ExecutionResult> promise = Mono.deferContextual(ctx -> Mono.fromFuture(graphQL.executeAsync(
                buildInput(params.graphQLRequest, params.request, ctx, contextFactory, dataLoaderRegistryFactory)))
        );
        return params.transportType.isEventStream() ? stream(params.graphQLRequest.getId(), promise) : promise.map(ExecutionResult::toSpecification);
    }

    private Publisher<ApolloMessage> stream(String id, Mono<ExecutionResult> result) {
        return result.flatMapMany(r -> r.getData() instanceof Publisher ? r.getData() : Flux.just(r))
                .map(next -> data(id, next))
                .onErrorResume(error -> Mono.just(error(id, error)))
                .concatWith(Mono.just(new ApolloMessage(id, GQL_COMPLETE)));
    }

    protected ApolloMessage data(String id, ExecutionResult result) {
        if (result.getErrors().isEmpty()) {
            return new DataMessage(id, result);
        }
        return new ErrorMessage(id,
                result.getErrors().stream()
                        .map(GraphQLError::toSpecification)
                        .collect(Collectors.toList()));
    }

    protected ApolloMessage error(String id, Throwable error) {
        return new ErrorMessage(id, Collections.singletonList(Collections.singletonMap("message", error.getMessage())));
    }
}
