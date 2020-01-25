package io.leangen.graphql.spqr.spring.modules.data;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLNonNull;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.OperationMapper;
import io.leangen.graphql.generator.mapping.SchemaTransformer;
import io.leangen.graphql.metadata.OperationArgument;

import java.lang.reflect.AnnotatedType;

public interface DefaultValueSchemaTransformer extends SchemaTransformer {

    @Override
    default GraphQLArgument transformArgument(GraphQLArgument argument, OperationArgument operationArgument, OperationMapper operationMapper, BuildContext buildContext) {
        if (supports(operationArgument.getJavaType()) && !(argument.getType() instanceof GraphQLNonNull) && argument.getDefaultValue() == null) {
            return argument.transform(builder -> builder.defaultValue(getDefaultValue()));
        }
        return argument;
    }

    Object getDefaultValue();

    boolean supports(AnnotatedType type);
}
