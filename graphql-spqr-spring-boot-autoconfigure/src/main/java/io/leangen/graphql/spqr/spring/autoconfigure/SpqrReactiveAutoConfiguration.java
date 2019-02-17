package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.FluxAdapter;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.MonoAdapter;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContext;
import io.leangen.graphql.spqr.spring.autoconfigure.reactive.ReactiveGlobalContextFactory;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.GuiController;
import io.leangen.graphql.spqr.spring.web.reactive.GraphQLReactiveController;
import io.leangen.graphql.spqr.spring.web.reactive.GraphQLReactiveExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    public ReactiveGlobalContextFactory globalContextFactory() {
        return params -> new ReactiveGlobalContext(params.getWebExchange());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public GraphQLExecutor<ServerWebExchange, CompletableFuture<Map<String, Object>>> graphQLExecutor(ReactiveGlobalContextFactory contextFactory) {
        return new GraphQLReactiveExecutor(contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(GraphQLController.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQLReactiveController graphQLController(GraphQL graphQL, GraphQLExecutor<ServerWebExchange, CompletableFuture<Map<String, Object>>> executor) {
        return new GraphQLReactiveController(graphQL, executor);
    }

    @Bean
    @ConditionalOnProperty(value = "graphql.spqr.gui.enabled", havingValue = "true")
    public GuiController guiController(SpqrProperties config) {
        return new GuiController(config);
    }
}
