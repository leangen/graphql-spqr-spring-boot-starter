package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.module.Module;

public class SpringDataModule implements Module {

    @Override
    public void setUp(SetupContext context) {
        ResolverBuilder sliceResolverBuilder = new SliceResolverBuilder();
        PageableAdapter pageableAdapter = new PageableAdapter();
        SortAdapter sortAdapter = new SortAdapter();
        RevisionSortAdapter revisionSortAdapter = new RevisionSortAdapter();
        context.getSchemaGenerator()
                .withNestedResolverBuilders((conf, builders) -> builders.append(sliceResolverBuilder))
                .withTypeAdapters(pageableAdapter, sortAdapter, new OrderAdapter(), revisionSortAdapter)
                .withSchemaTransformers(pageableAdapter, sortAdapter, revisionSortAdapter);
    }
}
