package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.ExtensionProvider;
import io.leangen.graphql.GeneratorConfiguration;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.generator.mapping.AbstractTypeAdapter;
import io.leangen.graphql.generator.mapping.ConverterSupportParams;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnBean(GraphQLSchema.class)
public class SpqrReactiveAutoConfiguration {

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private DataLoaderRegistryFactory dataLoaderRegistryFactory;


    @Bean
    @ConditionalOnMissingBean
    public GlobalContextFactory globalContextFactory() {
        return params -> new DefaultGlobalContext(params.getHttpRequest());
    }

    @Bean
//    @ConditionalOnProperty(name = "graphql.spqr.http.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQLController graphQLController(GraphQL graphQL, GlobalContextFactory contextFactory) {
        return new GraphQLController(graphQL, contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    public ExtensionProvider<GeneratorConfiguration, TypeMapper> typeMapperExtensionProvider() {
        return (config, defaults) -> defaults.append(Collections.singletonList(new MonoCompletableFutureAdapter()));
    }

    public class MonoCompletableFutureAdapter extends AbstractTypeAdapter<Mono, CompletableFuture> {


        @Override
        public CompletableFuture convertOutput(Mono original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
            return original.toFuture();
        }

        @Override
        public boolean supports(ConverterSupportParams params) {
            return false;
        }

        @Override
        public Mono convertInput(CompletableFuture substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
            return Mono.fromCompletionStage(substitute);
        }
    }

}
