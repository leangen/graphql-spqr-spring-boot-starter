package io.leangen.graphql.spqr.spring.util.mapping;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.OperationMapper;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.generator.mapping.common.ObjectTypeMapper;
import io.leangen.graphql.util.GraphQLUtils;
import org.springframework.data.domain.Page;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringPageMapper extends ObjectTypeMapper {

    @Override
    public GraphQLOutputType toGraphQLType(AnnotatedType javaType, OperationMapper operationMapper, Set<Class<? extends TypeMapper>> mappersToSkip, BuildContext buildContext) {
        AnnotatedType edgeType = GenericTypeReflector.getTypeParameter(javaType, Page.class.getTypeParameters()[0]);
        String connectionName = buildContext.typeInfoGenerator.generateTypeName(edgeType, buildContext.messageBundle) + "Connection";
        if (buildContext.typeCache.contains(connectionName)) {
            return new GraphQLTypeReference(connectionName);
        }
        buildContext.typeCache.register(connectionName);
        GraphQLOutputType type = operationMapper.toGraphQLType(edgeType, buildContext);
        List<GraphQLFieldDefinition> edgeFields = getFields(edgeType, buildContext, operationMapper).stream()
                .filter(field -> !GraphQLUtils.isRelayEdgeField(field))
                .collect(Collectors.toList());
        GraphQLObjectType edge = buildContext.relay.edgeType(type.getName(), type, null, edgeFields);
        List<GraphQLFieldDefinition> connectionFields = getFields(javaType, buildContext, operationMapper).stream()
                .filter(field -> !GraphQLUtils.isRelayConnectionField(field))
                .collect(Collectors.toList());

        return buildContext.relay.connectionType(type.getName(), edge, connectionFields);
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return GenericTypeReflector.isSuperType(Page.class, type.getType());

    }
}
