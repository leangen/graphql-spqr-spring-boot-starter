package io.leangen.graphql.spqr.spring.modules.reactive;

import graphql.schema.GraphQLInputType;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.OutputConverter;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.generator.mapping.TypeMappingEnvironment;
import io.leangen.graphql.generator.mapping.common.AbstractTypeSubstitutingMapper;
import io.leangen.graphql.util.ClassUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.Set;

public class MonoAdapter<T> extends AbstractTypeSubstitutingMapper<T> implements OutputConverter<Mono<T>, Object> {

    private final Scheduler scheduler;

    public MonoAdapter() {
        this (null);
    }

    public MonoAdapter(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public GraphQLInputType toGraphQLInputType(AnnotatedType javaType, Set<Class<? extends TypeMapper>> mappersToSkip, TypeMappingEnvironment env) {
        throw new UnsupportedOperationException(ClassUtils.getRawType(javaType.getType()).getSimpleName() + " can not be used as an input type");
    }

    @Override
    public Object convertOutput(Mono<T> original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        //For subscriptions, Mono<T> (Publisher<T>) should be returned directly
        if (resolutionEnvironment.dataFetchingEnvironment.getParentType() == resolutionEnvironment.dataFetchingEnvironment.getGraphQLSchema().getSubscriptionType()) {
            return original;
        }
        //For other operations it must be converted into a CompletableFuture<T>
        return scheduler != null ? original.subscribeOn(scheduler).toFuture() : original.toFuture();
    }

    @Override
    public AnnotatedType getSubstituteType(AnnotatedType original) {
        AnnotatedType innerType = GenericTypeReflector.getTypeParameter(original, Mono.class.getTypeParameters()[0]);
        return ClassUtils.addAnnotations(innerType, original.getAnnotations());
    }

    @Override
    public boolean supports(AnnotatedElement element, AnnotatedType type) {
        return GenericTypeReflector.isSuperType(Mono.class, type.getType());
    }
}
