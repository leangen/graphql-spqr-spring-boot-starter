package io.leangen.graphql.spqr.spring.modules.reactive;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.core.PublisherAdapter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.lang.reflect.AnnotatedType;

public class FluxAdapter<T> extends PublisherAdapter<T> {

    /*@Override
    public Flux<T> convertInput(CompletableFuture<List<T>> substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return Mono.fromFuture(substitute).flatMapMany(Flux::fromIterable);
    }*/

    @Override
    protected Object convertOutputForNonSubscription(Publisher<T> original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return ((Flux<T>)original).collectList().toFuture();
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return GenericTypeReflector.isSuperType(Flux.class, type.getType());
    }
}
