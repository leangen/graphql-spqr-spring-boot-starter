package io.leangen.graphql.spqr.spring.modules.security;

import io.leangen.graphql.module.Module;
import org.springframework.security.authentication.AuthenticationTrustResolver;

import java.util.Collections;

public class SpringSecurityModule implements Module {
    private final AuthenticationTrustResolver resolver;

    public SpringSecurityModule(AuthenticationTrustResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void setUp(SetupContext context) {
        context.getSchemaGenerator()
                .withResolverInterceptorFactories((config, factories) ->
                        factories.append(params -> Collections.singletonList(new AccessDeniedInterceptor(resolver))));
    }
}
