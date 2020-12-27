package io.leangen.graphql.spqr.spring.web.dto;

import java.lang.annotation.Annotation;

import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;

/**
 * @author Sergei Visotsky
 * @since 0.0.5
 */
public class ResolverBuilderBeanIdentity {

    private final Class<? extends ResolverBuilder> resolverType;
    private final String value;
    private final Class<? extends Annotation> qualifierType;

    public ResolverBuilderBeanIdentity(Class<? extends ResolverBuilder> resolverType, String value, Class<? extends Annotation> qualifierType) {
        this.resolverType = resolverType;
        this.value = value;
        this.qualifierType = qualifierType;
    }

    public String getValue() {
        return value;
    }

    public Class<? extends Annotation> getQualifierType() {
        return qualifierType;
    }

    public Class<? extends ResolverBuilder> getResolverType() {
        return resolverType;
    }
}
