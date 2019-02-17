package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.GuiController;
import io.leangen.graphql.spqr.spring.web.GraphQLMvcController;
import io.leangen.graphql.spqr.spring.web.GraphQLMvcExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpqrMvcAutoConfiguration {

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Bean
    @ConditionalOnMissingBean
    public GlobalContextFactory globalContextFactory() {
        return params -> new DefaultGlobalContext(params.getNativeRequest());
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public GraphQLExecutor<NativeWebRequest, Map<String, Object>> graphQLExecutor(GlobalContextFactory contextFactory) {
        return new GraphQLMvcExecutor(contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(GraphQLController.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQLMvcController graphQLController(GraphQL graphQL, GraphQLExecutor<NativeWebRequest, Map<String, Object>> executor) {
        return new GraphQLMvcController(graphQL, executor);
    }

    @Bean
    @ConditionalOnProperty(value = "graphql.spqr.gui.enabled", havingValue = "true")
    public GuiController guiController(SpqrProperties config) {
        return new GuiController(config);
    }
}
