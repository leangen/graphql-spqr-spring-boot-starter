package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.modules.security.SpringSecurityModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolver;

@Configuration
@ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
public class SpringSecurityAutoConfiguration {
    @Autowired(required = false)
    private AuthenticationTrustResolver resolver;

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.spring-security-compatible", havingValue = "true", matchIfMissing = true)
    public Internal<Module> springSecurityModule() {
        return new Internal<>(new SpringSecurityModule(resolver));
    }
}
