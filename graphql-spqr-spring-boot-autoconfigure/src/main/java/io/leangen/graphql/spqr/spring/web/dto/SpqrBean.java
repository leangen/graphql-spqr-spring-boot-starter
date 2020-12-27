package io.leangen.graphql.spqr.spring.web.dto;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

/**
 * Annotated bean data.
 *
 * @author Sergei Visotsky
 * @since 0.0.5
 */
public class SpqrBean {
    private final BeanScope scope;
    private final Object springBean;
    private final AnnotatedType type;
    private final List<ResolverBuilderBeanIdentity> resolverBuilders;

    public SpqrBean(ApplicationContext context, String beanName, AnnotatedType type) {
        this.springBean = context.getBean(beanName);
        this.scope = BeanScope.findBeanScope(context, beanName);
        this.type = type;
        this.resolverBuilders = new ArrayList<>();
    }

    public BeanScope getScope() {
        return scope;
    }

    public Object getSpringBean() {
        return springBean;
    }

    public AnnotatedType getType() {
        return type;
    }

    public List<ResolverBuilderBeanIdentity> getResolverBuilders() {
        return resolverBuilders;
    }
}
