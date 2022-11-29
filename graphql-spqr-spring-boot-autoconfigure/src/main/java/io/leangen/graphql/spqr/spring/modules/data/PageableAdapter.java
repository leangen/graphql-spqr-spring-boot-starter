package io.leangen.graphql.spqr.spring.modules.data;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNonNull;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.OperationMapper;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.metadata.InputField;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PageableAdapter extends AbstractTypeAdapter<Pageable, Pagination> implements DefaultValueSchemaTransformer {

    private final Pageable defaultPageable;

    public PageableAdapter(int defaultPageSize) {
        this.defaultPageable = defaultPageSize < 1 ? Pageable.unpaged() : PageRequest.of(0, defaultPageSize);
    }

    private static final Set<Class<?>> SUPPORTED_CLASSES = new HashSet<>(Arrays.asList(
            Pageable.class, PageRequest.class, AbstractPageRequest.class, Pageable.unpaged().getClass()
    ));

    @Override
    public Pageable convertInput(Pagination substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
        return substitute.toPageable();
    }

    @Override
    public Pagination convertOutput(Pageable original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return original.isPaged() ? new Pagination(original) : null;
    }

    @Override
    public Object getDefaultValue() {
        return defaultPageable;
    }

    @Override
    public GraphQLInputObjectField transformInputField(GraphQLInputObjectField field, InputField inputField, OperationMapper operationMapper, BuildContext buildContext) {
        if (field.getName().equals("pageSize") && field.getInputFieldDefaultValue() == null && !(field.getType() instanceof GraphQLNonNull)) {
            return defaultPageable.isPaged()
                    ? field.transform(builder -> builder.defaultValue(defaultPageable.getPageSize()))
                    : field.transform(builder -> builder.type(GraphQLNonNull.nonNull(field.getType())));
        }
        return field;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean supports(AnnotatedType type) {
        return SUPPORTED_CLASSES.contains(type.getType());
    }
}
