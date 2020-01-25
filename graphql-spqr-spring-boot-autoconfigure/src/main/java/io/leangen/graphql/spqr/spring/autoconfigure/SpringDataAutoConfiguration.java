package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.modules.data.SpringDataModule;
import io.leangen.graphql.spqr.spring.modules.data.SpringDataRelayModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.data.domain.Slice")
public class SpringDataAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.relay.spring-data-compatible", havingValue = "true")
    public Internal<Module> springDataRelayModule() {
        return new Internal<>(new SpringDataRelayModule());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.relay.spring-data-compatible", havingValue = "false", matchIfMissing = true)
    public Internal<Module> springDataModule() {
        return new Internal<>(new SpringDataModule());
    }
}
