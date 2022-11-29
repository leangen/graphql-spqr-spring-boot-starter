package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.modules.data.SpringDataModule;
import io.leangen.graphql.spqr.spring.modules.data.SpringDataRelayModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.data.domain.Slice")
public class SpringDataAutoConfiguration {

    @Value("${spring.data.web.pageable.default-page-size:20}")
    private int defaultPageSize;

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.relay.spring-data-compatible", havingValue = "true")
    public Internal<Module> springDataRelayModule() {
        return new Internal<>(new SpringDataRelayModule(defaultPageSize));
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.relay.spring-data-compatible", havingValue = "false", matchIfMissing = true)
    public Internal<Module> springDataModule() {
        return new Internal<>(new SpringDataModule(defaultPageSize));
    }
}
