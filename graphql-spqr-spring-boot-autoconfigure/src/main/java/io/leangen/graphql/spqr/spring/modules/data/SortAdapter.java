package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.Sort;

import java.lang.reflect.AnnotatedType;

public class SortAdapter extends AbstractTypeAdapter<Sort, Sorting> implements DefaultValueSchemaTransformer {

    @Override
    public Sort convertInput(Sorting substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return substitute.toSort();
    }

    @Override
    public Sorting convertOutput(Sort original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return new Sorting(original);
    }

    @Override
    public Object getDefaultValue() {
        return Sort.unsorted();
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return Sort.class.equals(type.getType());
    }
}
