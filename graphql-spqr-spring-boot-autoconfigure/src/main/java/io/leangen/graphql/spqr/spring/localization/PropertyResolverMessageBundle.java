package io.leangen.graphql.spqr.spring.localization;

import io.leangen.graphql.metadata.messages.MessageBundle;
import org.springframework.core.env.PropertyResolver;

public class PropertyResolverMessageBundle implements MessageBundle {

    private final PropertyResolver propertyResolver;

    public PropertyResolverMessageBundle(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    @Override
    public String getMessage(String key) {
        return propertyResolver.getProperty(key);
    }

    @Override
    public boolean containsKey(String key) {
        return propertyResolver.containsProperty(key);
    }

    @Override
    public String interpolate(String template) {
        return propertyResolver.resolvePlaceholders(template);
    }
}
