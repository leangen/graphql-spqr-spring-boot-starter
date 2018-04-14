package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.DefaultGraphQLController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "graphql.spqr.default-endpoint.disable", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class SpqrMvcAutoConfiguration {
    @Bean
    public DefaultGraphQLController defaultGraphQLController(GraphQLSchema schema) {
        return new DefaultGraphQLController(schema);
    }
}
