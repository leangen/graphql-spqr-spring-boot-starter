package io.leangen.graphql.spqr.spring.annotations;

import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(WithResolverBuilders.class)
public @interface WithResolverBuilder {
    Class<? extends ResolverBuilder> value();
    String qualifierValue() default "";
    Class<? extends Annotation> qualifierType() default Qualifier.class;
}
