package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilderParams;
import io.leangen.graphql.util.ClassUtils;
import org.springframework.data.domain.Slice;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class SliceResolverBuilder extends BeanResolverBuilder {

    private static final List<String> ALLOWED_PREFIXES = Arrays.asList("has", "next", "previous");

    @Override
    protected boolean isQuery(Method method, ResolverBuilderParams params) {
        return ALLOWED_PREFIXES.stream().anyMatch(prefix -> method.getName().startsWith(prefix))
                || super.isQuery(method, params);
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return ClassUtils.isSuperClass(Slice.class, type);
    }
}
