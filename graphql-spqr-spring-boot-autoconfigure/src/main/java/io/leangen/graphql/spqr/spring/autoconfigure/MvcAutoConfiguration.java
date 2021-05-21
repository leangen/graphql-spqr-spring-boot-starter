package io.leangen.graphql.spqr.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.mvc.GraphQLMvcExecutor;
import io.leangen.graphql.spqr.spring.web.GuiController;
import io.leangen.graphql.spqr.spring.web.mvc.DefaultGraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.mvc.DefaultGraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.leangen.graphql.spqr.spring.autoconfigure.SpqrProperties.Http.Mvc.Executor.BLOCKING;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MvcAutoConfiguration {

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Bean
    @ConditionalOnMissingBean
    public MvcContextFactory globalContextFactory() {
        return params -> new DefaultGlobalContext<>(params.getNativeRequest());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public GraphQLMvcExecutor defaultExecutor(MvcContextFactory contextFactory, SpqrProperties config) {
        DefaultGraphQLExecutor defaultExecutor = new DefaultGraphQLExecutor(contextFactory, dataLoaderRegistryFactory);
        return config.getHttp().getMvc().getExecutor() == BLOCKING ? defaultExecutor.blocking() : defaultExecutor;
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(GraphQLController.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public DefaultGraphQLController graphQLController(GraphQL graphQL, GraphQLMvcExecutor executor, ObjectMapper objectMapper) {
        return new DefaultGraphQLController(graphQL, executor, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(value = "graphql.spqr.gui.enabled", havingValue = "true")
    public GuiController guiController(SpqrProperties config) {
        return new GuiController(config);
    }
}
