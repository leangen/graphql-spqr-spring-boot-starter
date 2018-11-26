package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GuiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpqrMvcAutoConfiguration {

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Bean
    @ConditionalOnMissingBean
    public GlobalContextFactory globalContextFactory() {
        return params -> new DefaultGlobalContext(params.getHttpRequest());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQLController graphQLController(GraphQL graphQL, GlobalContextFactory contextFactory) {
        return new GraphQLController(graphQL, contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "graphql.spqr.gui.enabled", havingValue = "true")
    public GuiController guiController(SpqrProperties config) {
        return new GuiController(config);
    }
}
