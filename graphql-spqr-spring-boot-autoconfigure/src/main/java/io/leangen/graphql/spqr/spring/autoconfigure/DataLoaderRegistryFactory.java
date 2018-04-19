package io.leangen.graphql.spqr.spring.autoconfigure;

import org.dataloader.DataLoaderRegistry;

public interface DataLoaderRegistryFactory {

    DataLoaderRegistry createDataLoaderRegistry();
}
