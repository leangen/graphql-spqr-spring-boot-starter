package io.leangen.graphql.spqr.spring.modules.reactive;

import io.leangen.graphql.module.Module;

public class ReactorModule implements Module {

    @Override
    public void setUp(SetupContext context) {
        MonoAdapter<?> monoAdapter = new MonoAdapter<>();
        FluxAdapter<?> fluxAdapter = new FluxAdapter<>();

        context.getSchemaGenerator()
                .withTypeMappers(monoAdapter, fluxAdapter)
                .withOutputConverters(monoAdapter, fluxAdapter)
                .withSchemaTransformers(fluxAdapter);
    }
}
