package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.generic.PageFactory;
import io.leangen.graphql.generator.mapping.OutputConverter;
import io.leangen.graphql.generator.mapping.common.AbstractTypeSubstitutingMapper;
import io.leangen.graphql.util.ClassUtils;
import org.springframework.data.domain.Slice;

import java.lang.reflect.AnnotatedType;

public class PageToConnectionAdapter<T> extends AbstractTypeSubstitutingMapper<io.leangen.graphql.execution.relay.Page<T>>
        implements OutputConverter<Slice<T>, io.leangen.graphql.execution.relay.Page<T>> {

    @Override
    public io.leangen.graphql.execution.relay.Page<T> convertOutput(Slice<T> original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
        return PageFactory.createOffsetBasedPage(original.getContent(), original.getPageable().getOffset(),
                original.hasNext(), original.hasPrevious());
    }

    @Override
    public AnnotatedType getSubstituteType(AnnotatedType original) {
        AnnotatedType itemType = GenericTypeReflector.getTypeParameter(original, Slice.class.getTypeParameters()[0]);
        return TypeFactory.parameterizedAnnotatedClass(io.leangen.graphql.execution.relay.Page.class, original.getAnnotations(), itemType);
    }

    @Override
    public boolean supports(AnnotatedType type) {
        return ClassUtils.isSuperClass(Slice.class, type);
    }
}
