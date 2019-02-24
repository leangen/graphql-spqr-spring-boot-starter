package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.FluxAdapter;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.MonoAdapter;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.reactive.GraphQLReactiveExecutor;
import io.leangen.graphql.spqr.spring.web.GuiController;
import io.leangen.graphql.spqr.spring.web.reactive.DefaultGraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.reactive.DefaultGraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SpqrReactiveAutoConfiguration {

    @Bean
    public Internal<Module> reactorModule() {
        MonoAdapter monoAdapter = new MonoAdapter();
        FluxAdapter fluxAdapter = new FluxAdapter();
        return new Internal<>(context -> context.getSchemaGenerator()
                .withTypeMappers(monoAdapter, fluxAdapter)
                .withOutputConverters(monoAdapter, fluxAdapter)
                .withSchemaTransformers(fluxAdapter));
    }

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Bean
    @ConditionalOnMissingBean
    public ReactiveContextFactory globalContextFactory() {
        return params -> new DefaultGlobalContext<>(params.getNativeRequest());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public GraphQLReactiveExecutor graphQLExecutor(ReactiveContextFactory contextFactory) {
        return new DefaultGraphQLExecutor(contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(GraphQLController.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public DefaultGraphQLController graphQLController(GraphQL graphQL, GraphQLReactiveExecutor executor) {
        return new DefaultGraphQLController(graphQL, executor);
    }

    @Bean
    @ConditionalOnProperty(value = "graphql.spqr.gui.enabled", havingValue = "true")
    public GuiController guiController(SpqrProperties config) {
        return new GuiController(config);
    }
}
