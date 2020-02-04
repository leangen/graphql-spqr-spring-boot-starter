package io.leangen.graphql.spqr.spring.modules.reactive;

import io.leangen.graphql.execution.ResolverInterceptor;
import io.leangen.graphql.execution.ResolverInterceptorFactory;
import io.leangen.graphql.execution.ResolverInterceptorFactoryParams;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.util.ClassUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

public class ReactorModule implements Module {

    @Override
    public void setUp(SetupContext context) {
        MonoAdapter<?> monoAdapter = new MonoAdapter<>();
        FluxAdapter<?> fluxAdapter = new FluxAdapter<>();

        context.getSchemaGenerator()
                .withTypeMappers(monoAdapter, fluxAdapter)
                .withOutputConverters(monoAdapter, fluxAdapter)
                .withSchemaTransformers(fluxAdapter)
                .withResolverInterceptorFactories((config, factories) -> factories.append(new InterceptorFactory()));
    }

    private static class InterceptorFactory implements ResolverInterceptorFactory {

        @Override
        public List<ResolverInterceptor> getInterceptors(ResolverInterceptorFactoryParams params) {
            Class<?> returnType = ClassUtils.getRawType(params.getResolver().getReturnType().getType());
            if (Flux.class.isAssignableFrom(returnType)) {
                return Collections.singletonList(new FluxInterceptor());
            }
            if (Mono.class.isAssignableFrom(returnType)) {
                return Collections.singletonList(new MonoInterceptor());
            }
            return Collections.emptyList();
        }
    }
}
