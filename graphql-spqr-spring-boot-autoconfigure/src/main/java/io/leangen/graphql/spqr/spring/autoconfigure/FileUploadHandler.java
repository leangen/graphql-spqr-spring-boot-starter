package io.leangen.graphql.spqr.spring.autoconfigure;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.util.*;

import graphql.schema.*;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.generator.mapping.ArgumentInjector;
import io.leangen.graphql.generator.mapping.ArgumentInjectorParams;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.generator.mapping.TypeMappingEnvironment;
import io.leangen.graphql.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;

class FileUploadHandler implements TypeMapper, ArgumentInjector {

    public static final GraphQLScalarType FILE_UPLOAD_SCALAR = GraphQLScalarType.newScalar()
            .name("FileUpload")
            .description("An apollo upload compatible scalar for multipart uploads")
            .coercing(new Coercing<MultipartFile, Void>() {

                @Override
                public Void serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    throw new CoercingSerializeException("Upload is not a return type");
                }

                @Override
                public MultipartFile parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof MultipartFile) {
                        return (MultipartFile) input;
                    }
                    throw new CoercingParseValueException("Expected the input to be parsed by the servlet controller");
                }

                @Override
                public MultipartFile parseLiteral(Object input) throws CoercingParseLiteralException {
                    throw new CoercingParseLiteralException("Parsing the literal of the upload is not supported");
                }
            })
            .build();

    @Override
    public GraphQLInputType toGraphQLInputType(AnnotatedType javaType, Set<Class<? extends TypeMapper>> mappersToSkip, TypeMappingEnvironment env) {
        return FILE_UPLOAD_SCALAR;
    }

    @Override
    public GraphQLOutputType toGraphQLType(AnnotatedType javaType, Set<Class<? extends TypeMapper>> mappersToSkip, TypeMappingEnvironment env) {
        throw new UnsupportedOperationException("FileUpload is not an output type");
    }

    @Override
    public boolean supports(AnnotatedElement element, AnnotatedType type) {
        return type != null && ClassUtils.isAssignable(MultipartFile.class, type.getType());
    }

    @Override
    public Object getArgumentValue(ArgumentInjectorParams params) {
        if ((params.getInput() instanceof MultipartFile)) {
            return params.getInput();
        }
        if (!(params.getInput() instanceof Collection)) {
            return null;
        }
        if (ClassUtils.isAssignable(params.getType().getType(), params.getInput().getClass())) {
            return params.getInput();
        }
        if (ClassUtils.isAssignable(List.class, params.getType().getType())) {
            //noinspection rawtypes,unchecked
            return new ArrayList((Collection) params.getInput());
        }
        if (ClassUtils.isAssignable(Set.class, params.getType().getType())) {
            //noinspection rawtypes,unchecked
            return new LinkedHashSet((Collection) params.getInput());
        }
        throw new UnsupportedOperationException("Cannot convert " + params.getInput().getClass() + " to " + params.getType());
    }

    @Override
    public boolean supports(AnnotatedType type, Parameter parameter) {
        return supports(null, type)
                || ClassUtils.isAssignable(Iterable.class, type.getType())
                && supports(null, GenericTypeReflector.getTypeParameter(type, Iterable.class.getTypeParameters()[0]));
    }
}

