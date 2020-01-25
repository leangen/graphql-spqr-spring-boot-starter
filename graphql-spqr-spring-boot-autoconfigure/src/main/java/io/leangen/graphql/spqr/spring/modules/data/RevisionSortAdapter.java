package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.RevisionSort;

import java.lang.reflect.AnnotatedType;

public class RevisionSortAdapter extends AbstractTypeAdapter<RevisionSort, Sort.Direction> implements DefaultValueSchemaTransformer {

    @Override
    public RevisionSort convertInput(Sort.Direction substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return substitute.isAscending() ? RevisionSort.asc() : RevisionSort.desc();
    }

    @Override
    public Sort.Direction convertOutput(RevisionSort original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return RevisionSort.getRevisionDirection(original);
    }

    @Override
    public Object getDefaultValue() {
        return RevisionSort.asc();
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return RevisionSort.class.equals(type.getType());
    }
}
