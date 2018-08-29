package io.leangen.graphql.spqr.spring.localization;

import io.leangen.graphql.metadata.messages.MessageBundle;
import org.springframework.core.env.Environment;

public class EnvironmentMessageBundle implements MessageBundle {
    private final Environment environment;

    public EnvironmentMessageBundle(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getMessage(String key) {
        return environment.getProperty(key);
    }
}
