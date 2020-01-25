package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PageableAdapter extends AbstractTypeAdapter<Pageable, Pagination> implements DefaultValueSchemaTransformer {

    private static final Set<Class<?>> supportedClasses = new HashSet<>(Arrays.asList(
            Pageable.class, PageRequest.class, AbstractPageRequest.class, Pageable.unpaged().getClass()
    ));

    @Override
    public Pageable convertInput(Pagination substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return substitute.toPageable();
    }

    @Override
    public Pagination convertOutput(Pageable original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return original == Pageable.unpaged() ? null : new Pagination(original);
    }

    @Override
    public Object getDefaultValue() {
        return PageRequest.of(0, 10);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean supports(AnnotatedType type) {
        return supportedClasses.contains(type.getType());
    }
}
