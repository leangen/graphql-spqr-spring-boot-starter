package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.DefaultGraphQLController;
import io.leangen.graphql.spqr.spring.web.DefaultGraphiQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    @ConditionalOnProperty(name = "graphql.spqr.default-endpoint.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(GraphQLSchema.class)
    public DefaultGraphQLController defaultGraphQLController(GraphQL graphQL) {
        return new DefaultGraphQLController(graphQL, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "graphiql.enabled", havingValue = "true")
    public DefaultGraphiQLController graphiQLController() {
        return new DefaultGraphiQLController();
    }
}
