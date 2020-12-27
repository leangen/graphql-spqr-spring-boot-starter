package io.leangen.graphql.spqr.spring.web.dto;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Scope in which Spring bean {@link Bean} should be created
 *
 * @author Sergei Visotsky
 * @since 0.0.5
 */
public enum BeanScope {
    SINGLETON,
    PROTOTYPE;

    public static BeanScope findBeanScope(ApplicationContext context, String beanName) {
        if (context.isSingleton(beanName)) {
            return BeanScope.SINGLETON;
        } else if (context.isPrototype(beanName)) {
            return BeanScope.PROTOTYPE;
        } else {
            //TODO log warning and proceed
            throw new RuntimeException("Unsupported bean scope");
        }
    }
}
