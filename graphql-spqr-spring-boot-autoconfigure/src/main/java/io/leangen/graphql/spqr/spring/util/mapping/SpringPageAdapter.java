package io.leangen.graphql.spqr.spring.util.mapping;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.generic.PageFactory;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.Page;

import java.lang.reflect.AnnotatedType;

public class SpringPageAdapter extends AbstractTypeAdapter<Page, io.leangen.graphql.execution.relay.Page> {
    @Override
    public Page convertInput(io.leangen.graphql.execution.relay.Page substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public io.leangen.graphql.execution.relay.Page convertOutput(Page original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return PageFactory.createOffsetBasedPage(
                original.getContent().subList(original.getNumber(), original.getPageable().getPageSize() + original.getNumber()),
                original.getTotalElements(), original.getPageable().getOffset());
    }
}
