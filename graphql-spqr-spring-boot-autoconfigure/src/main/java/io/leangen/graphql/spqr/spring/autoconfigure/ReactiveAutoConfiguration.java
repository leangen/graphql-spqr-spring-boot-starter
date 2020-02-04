package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.modules.reactive.ReactorModule;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GuiController;
import io.leangen.graphql.spqr.spring.web.reactive.DefaultGraphQLController;
import io.leangen.graphql.spqr.spring.web.reactive.DefaultGraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.reactive.GraphQLReactiveExecutor;
import io.leangen.graphql.spqr.spring.web.reactive.WebFluxContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.context.Context;

import java.util.Optional;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveAutoConfiguration {

    @Bean
    public Internal<Module> reactorModule() {
        return new Internal<>(new ReactorModule());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveContextFactory globalContextFactory() {
        return params -> new WebFluxContext(params.getNativeRequest(), (Context) params.getEnvironment());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public GraphQLReactiveExecutor graphQLExecutor(ReactiveContextFactory contextFactory, Optional<DataLoaderRegistryFactory> dataLoaderRegistryFactory) {
        return new DefaultGraphQLExecutor(contextFactory, dataLoaderRegistryFactory.orElse(null));
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
