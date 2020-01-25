package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.module.Module;

public class SpringDataRelayModule implements Module {

    @Override
    public void setUp(SetupContext context) {
        PageToConnectionAdapter<?> pageAdapter = new PageToConnectionAdapter<>();
        PageableAdapter pageableAdapter = new PageableAdapter();
        SortAdapter sortAdapter = new SortAdapter();
        RevisionSortAdapter revisionSortAdapter = new RevisionSortAdapter();
        context.getSchemaGenerator()
                .withRelayConnectionCheckRelaxed()
                .withTypeMappers(pageAdapter)
                .withOutputConverters(pageAdapter)
                .withTypeAdapters(pageableAdapter, sortAdapter, new OrderAdapter(), revisionSortAdapter)
                .withSchemaTransformers(pageableAdapter, sortAdapter, revisionSortAdapter);
    }
}
