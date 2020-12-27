package io.leangen.graphql.spqr.spring.autoconfigure;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.ExtendedGeneratorConfiguration;
import io.leangen.graphql.ExtensionProvider;
import io.leangen.graphql.GeneratorConfiguration;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.execution.ResolverInterceptorFactory;
import io.leangen.graphql.generator.mapping.ArgumentInjector;
import io.leangen.graphql.generator.mapping.InputConverter;
import io.leangen.graphql.generator.mapping.OutputConverter;
import io.leangen.graphql.generator.mapping.SchemaTransformer;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.generator.mapping.strategy.AbstractInputHandler;
import io.leangen.graphql.generator.mapping.strategy.InterfaceMappingStrategy;
import io.leangen.graphql.metadata.messages.MessageBundle;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.metadata.strategy.type.TypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.value.InputFieldBuilder;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.web.dto.BeanScope;
import io.leangen.graphql.spqr.spring.web.dto.SpqrBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(GraphQLSchemaGenerator.class)
@EnableConfigurationProperties(SpqrProperties.class)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class BaseAutoConfiguration {

    private final ConfigurableApplicationContext context;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, ResolverBuilder> globalResolverBuilderExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, TypeMapper> typeMapperExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, InputConverter> inputConverterExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, OutputConverter> outputConverterExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, ArgumentInjector> argumentInjectorExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, SchemaTransformer> schemaTransformerExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, ResolverInterceptorFactory> resolverInterceptorFactoryExtensionProvider;

    @Autowired(required = false)
    private ValueMapperFactory valueMapperFactory;

    @Autowired(required = false)
    private ExtensionProvider<ExtendedGeneratorConfiguration, InputFieldBuilder> inputFieldBuilderProvider;

    @Autowired(required = false)
    private TypeInfoGenerator typeInfoGenerator;

    @Autowired(required = false)
    private AbstractInputHandler abstractInputHandler;

    @Autowired(required = false)
    private InclusionStrategy inclusionStrategy;

    @Autowired(required = false)
    private InterfaceMappingStrategy interfaceMappingStrategy;

    @Autowired(required = false)
    private Set<MessageBundle> messageBundles;

    @Autowired(required = false)
    private ExtensionProvider<GeneratorConfiguration, Module> moduleExtensionProvider;

    @Autowired(required = false)
    private List<Internal<Module>> internalModules;

    @Autowired
    public BaseAutoConfiguration(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Bean
    @ConditionalOnMissingBean
    public AnnotatedResolverBuilder defaultAnnotatedResolverBuilder() {
        return new AnnotatedResolverBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanResolverBuilder defaultBeanResolverBuilder() {
        return new BeanResolverBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public PublicResolverBuilder defaultPublicResolverBuilder() {
        return new PublicResolverBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchemaGenerator graphQLSchemaGenerator(SpqrProperties spqrProperties) {
        GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator();

        schemaGenerator.withBasePackages(spqrProperties.getBasePackages());

        if (spqrProperties.getRelay().isEnabled()) {
            if (!StringUtils.isEmpty(spqrProperties.getRelay().getMutationWrapper())) {
                schemaGenerator.withRelayCompliantMutations(
                        spqrProperties.getRelay().getMutationWrapper(), spqrProperties.getRelay().getMutationWrapperDescription()
                );
            } else {
                schemaGenerator.withRelayCompliantMutations();
            }
        }

        Map<String, SpqrBean> beansWiredAsComponent = graphQLApiFactory().findGraphQLApiComponents();
        addOperationSources(schemaGenerator, beansWiredAsComponent.values());

        List<SpqrBean> beansWiredWithAsBeans = graphQLApiFactory().findGraphQLApiBeans();
        addOperationSources(schemaGenerator, beansWiredWithAsBeans);

        // Modules should be registered first, so that extension providers have a chance to override what they need
        // Built-in modules must go before the user-provided ones for similar reasons
        if (internalModules != null) {
            internalModules.forEach(module -> schemaGenerator.withModules(module.get()));
        }

        if (moduleExtensionProvider != null) {
            schemaGenerator.withModules(moduleExtensionProvider);
        }

        if (globalResolverBuilderExtensionProvider != null) {
            schemaGenerator.withResolverBuilders(globalResolverBuilderExtensionProvider);
        }

        if (typeMapperExtensionProvider != null) {
            schemaGenerator.withTypeMappers(typeMapperExtensionProvider);
        }

        if (inputConverterExtensionProvider != null) {
            schemaGenerator.withInputConverters(inputConverterExtensionProvider);
        }

        if (outputConverterExtensionProvider != null) {
            schemaGenerator.withOutputConverters(outputConverterExtensionProvider);
        }

        if (argumentInjectorExtensionProvider != null) {
            schemaGenerator.withArgumentInjectors(argumentInjectorExtensionProvider);
        }

        if (schemaTransformerExtensionProvider != null) {
            schemaGenerator.withSchemaTransformers(schemaTransformerExtensionProvider);
        }

        if (resolverInterceptorFactoryExtensionProvider != null) {
            schemaGenerator.withResolverInterceptorFactories(resolverInterceptorFactoryExtensionProvider);
        }

        if (valueMapperFactory != null) {
            schemaGenerator.withValueMapperFactory(valueMapperFactory);
        }

        if (inputFieldBuilderProvider != null) {
            schemaGenerator.withInputFieldBuilders(inputFieldBuilderProvider);
        }

        if (typeInfoGenerator != null) {
            schemaGenerator.withTypeInfoGenerator(typeInfoGenerator);
        }

        if (spqrProperties.isAbstractInputTypeResolution()) {
            schemaGenerator.withAbstractInputTypeResolution();
        }

        if (abstractInputHandler != null) {
            schemaGenerator.withAbstractInputHandler(abstractInputHandler);
        }

        if (messageBundles != null && !messageBundles.isEmpty()) {
            schemaGenerator.withStringInterpolation(messageBundles.toArray(new MessageBundle[0]));
        }

        if (inclusionStrategy != null) {
            schemaGenerator.withInclusionStrategy(inclusionStrategy);
        }

        if (interfaceMappingStrategy != null) {
            schemaGenerator.withInterfaceMappingStrategy(interfaceMappingStrategy);
        }

        if (spqrProperties.getRelay().isConnectionCheckRelaxed()) {
            schemaGenerator.withRelayConnectionCheckRelaxed();
        }

        return schemaGenerator;
    }

    private void addOperationSources(GraphQLSchemaGenerator schemaGenerator, Collection<SpqrBean> spqrBeans) {
        spqrBeans.stream()
                .filter(spqrBean -> spqrBean.getScope().equals(BeanScope.SINGLETON))
                .forEach(
                        spqrBean ->
                                schemaGenerator.withOperationsFromSingleton(
                                        spqrBean.getSpringBean(),
                                        spqrBean.getType(),
                                        spqrBean.getResolverBuilders().stream()
                                                .map(resolverBuilderBeanIdentity -> graphQLApiFactory().findQualifiedBeanByType(resolverBuilderBeanIdentity.getResolverType(),
                                                        resolverBuilderBeanIdentity.getValue(),
                                                        resolverBuilderBeanIdentity.getQualifierType()))
                                                .toArray(ResolverBuilder[]::new)
                                )
                );
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLApiFactory graphQLApiFactory() {
        return new GraphQLApiFactory(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchema graphQLSchema(GraphQLSchemaGenerator schemaGenerator) {
        return schemaGenerator.generate();
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQL graphQL(GraphQLSchema schema) {
        GraphQL.Builder builder = GraphQL.newGraphQL(schema);
        return builder.build();
    }
}
