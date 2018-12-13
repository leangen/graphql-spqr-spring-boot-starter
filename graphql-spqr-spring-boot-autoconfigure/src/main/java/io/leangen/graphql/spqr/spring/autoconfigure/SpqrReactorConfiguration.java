package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.reactor.FluxAdapter;
import io.leangen.graphql.spqr.spring.reactor.MonoAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
@ConditionalOnClass(Flux.class)
public class SpqrReactorConfiguration {

    @Bean
    public Internal<Module> reactorModule() {
        MonoAdapter monoAdapter = new MonoAdapter();
        FluxAdapter fluxAdapter = new FluxAdapter();
        return new Internal<>(context -> context.getSchemaGenerator()
                .withTypeMappers(monoAdapter, fluxAdapter)
                .withOutputConverters(monoAdapter, fluxAdapter)
                .withSchemaTransformers(fluxAdapter));
    }
}
