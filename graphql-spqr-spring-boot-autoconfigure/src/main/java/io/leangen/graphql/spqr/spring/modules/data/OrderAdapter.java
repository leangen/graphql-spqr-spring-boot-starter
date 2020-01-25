package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.Sort;

import java.lang.reflect.AnnotatedType;

public class OrderAdapter extends AbstractTypeAdapter<Sort.Order, Order> {

    @Override
    public Sort.Order convertInput(Order substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return substitute.toOrder();
    }

    @Override
    public Order convertOutput(Sort.Order original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return new Order(original);
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return Sort.Order.class.equals(type.getType());
    }
}
