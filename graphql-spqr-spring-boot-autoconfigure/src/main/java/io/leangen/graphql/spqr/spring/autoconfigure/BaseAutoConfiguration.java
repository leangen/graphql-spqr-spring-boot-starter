package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.ExtendedGeneratorConfiguration;
import io.leangen.graphql.ExtensionProvider;
import io.leangen.graphql.GeneratorConfiguration;
import io.leangen.graphql.GraphQLRuntime;
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
import io.leangen.graphql.metadata.strategy.query.AbstractResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.MethodInvokerFactory;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.metadata.strategy.type.TypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.value.InputFieldBuilder;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilder;
import io.leangen.graphql.util.Utils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnClass(GraphQLSchemaGenerator.class)
@EnableConfigurationProperties(SpqrProperties.class)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class BaseAutoConfiguration {

    private final ConfigurableApplicationContext context;
    private final MethodInvokerFactory aopAwareFactory = new AopAwareMethodInvokerFactory();

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
        return (AnnotatedResolverBuilder) new AnnotatedResolverBuilder().withMethodInvokerFactory(aopAwareFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanResolverBuilder defaultBeanResolverBuilder() {
        return (BeanResolverBuilder) new BeanResolverBuilder().withMethodInvokerFactory(aopAwareFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public PublicResolverBuilder defaultPublicResolverBuilder() {
        return (PublicResolverBuilder) new PublicResolverBuilder().withMethodInvokerFactory(aopAwareFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchemaGenerator graphQLSchemaGenerator(SpqrProperties spqrProperties) {
        GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator();

        schemaGenerator.withBasePackages(spqrProperties.getBasePackages());

        if (spqrProperties.getRelay().isEnabled()) {
            if (Utils.isNotEmpty(spqrProperties.getRelay().getMutationWrapper())) {
                schemaGenerator.withRelayCompliantMutations(
                        spqrProperties.getRelay().getMutationWrapper(), spqrProperties.getRelay().getMutationWrapperDescription()
                );
            } else {
                schemaGenerator.withRelayCompliantMutations();
            }
        }

        Map<String, SpqrBean> apiComponents = findGraphQLApiComponents();
        addOperationSources(schemaGenerator, apiComponents.values());

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
        } else {
            schemaGenerator.withResolverBuilders(defaultAnnotatedResolverBuilder());
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
        spqrBeans.forEach(spqrBean ->
                schemaGenerator.withOperationsFromBean(
                        spqrBean.beanSupplier,
                        spqrBean.type,
                        spqrBean.exposedType,
                        spqrBean.resolverBuilders.stream()
                                .map(criteria -> findQualifiedBeanByType(
                                        criteria.getResolverType(),
                                        criteria.getValue(),
                                        criteria.getQualifierType()))
                                .peek(resolverBuilder -> {
                                    if (resolverBuilder instanceof AbstractResolverBuilder) {
                                        ((AbstractResolverBuilder) resolverBuilder).withMethodInvokerFactory(aopAwareFactory);
                                    }
                                })
                                .toArray(ResolverBuilder[]::new)
                )
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchema graphQLSchema(GraphQLSchemaGenerator schemaGenerator) {
        return schemaGenerator.generate();
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQL graphQL(GraphQLSchema schema, SpqrProperties spqrProperties, List<Instrumentation> instrumentations) {
        GraphQLRuntime.Builder builder = GraphQLRuntime.newGraphQL(schema);
        instrumentations.forEach(builder::instrumentation);
        if (spqrProperties.getMaxComplexity() > 1) {
            builder.maximumQueryComplexity(spqrProperties.getMaxComplexity());
        }
        return builder.build();
    }

    private <T> T findQualifiedBeanByType(Class<? extends T> type, String qualifierValue, Class<? extends Annotation> qualifierType) {
        final NoSuchBeanDefinitionException noSuchBeanDefinitionException = new NoSuchBeanDefinitionException(qualifierValue, "No matching " + type.getSimpleName() +
                " bean found for qualifier " + qualifierValue + " of type " + qualifierType.getSimpleName() + " !");
        try {
            if (Utils.isEmpty(qualifierValue)) {
                if (qualifierType.equals(Qualifier.class)) {
                    return Optional.of(
                            context.getBean(type))
                            .orElseThrow(() -> noSuchBeanDefinitionException);
                }
                return context.getBean(
                        Arrays.stream(context.getBeanNamesForAnnotation(qualifierType))
                                .filter(beanName -> type.isInstance(context.getBean(beanName)))
                                .findFirst()
                                .orElseThrow(() -> noSuchBeanDefinitionException),
                        type);
            }

            return BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getBeanFactory(), type, qualifierValue);
        } catch (NoSuchBeanDefinitionException noBeanException) {
            ConfigurableListableBeanFactory factory = context.getBeanFactory();

            for (String name : factory.getBeanDefinitionNames()) {
                BeanDefinition bd = factory.getBeanDefinition(name);

                if (bd.getSource() instanceof StandardMethodMetadata) {
                    StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                    if (metadata.getReturnTypeName().equals(type.getName())) {
                        Map<String, Object> attributes = metadata.getAnnotationAttributes(qualifierType.getName());
                        if (null != attributes) {
                            if (qualifierType.equals(Qualifier.class)) {
                                if (qualifierValue.equals(attributes.get("value"))) {
                                    return context.getBean(name, type);
                                }
                            }
                            return context.getBean(name, type);
                        }
                    }
                }
            }

            throw noSuchBeanDefinitionException;
        }
    }

    private Map<String, SpqrBean> findGraphQLApiComponents() {
        final String[] apiBeanNames = context.getBeanNamesForAnnotation(GraphQLApi.class);
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        Map<String, SpqrBean> result = new HashMap<>();
        for (String beanName : apiBeanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            AnnotatedType beanType;
            Set<WithResolverBuilder> resolverBuilders;
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                beanType = metadata.getIntrospectedMethod().getAnnotatedReturnType();
                resolverBuilders = AnnotatedElementUtils.findMergedRepeatableAnnotations(metadata.getIntrospectedMethod(), WithResolverBuilder.class);
            } else {
                BeanDefinition current = beanDefinition;
                BeanDefinition originatingBeanDefinition = current;
                while (current != null) {
                    originatingBeanDefinition = current;
                    current = current.getOriginatingBeanDefinition();
                }
                ResolvableType resolvableType = originatingBeanDefinition.getResolvableType();
                if (resolvableType != ResolvableType.NONE && Utils.isNotEmpty(originatingBeanDefinition.getBeanClassName())
                        //Sanity check only -- should never happen
                        && !originatingBeanDefinition.getBeanClassName().startsWith("org.springframework.")) {
                    beanType = GenericTypeReflector.annotate(resolvableType.getType());
                } else {
                    beanType = GenericTypeReflector.annotate(AopUtils.getTargetClass(context.getBean(beanName)));
                }
                resolverBuilders = AnnotatedElementUtils.findMergedRepeatableAnnotations(beanType, WithResolverBuilder.class);
            }
            List<ResolverBuilderBeanCriteria> builders = resolverBuilders.stream()
                    .map(builder -> new ResolverBuilderBeanCriteria(builder.value(), builder.qualifierValue(), builder.qualifierType()))
                    .collect(Collectors.toList());
            result.put(beanName, new SpqrBean(context, beanName, beanType, builders));
        }
        return result;
    }

    private static class SpqrBean {

        final BeanScope scope;
        final Supplier<Object> beanSupplier;
        final AnnotatedType type;
        final Class<?> exposedType;
        final List<ResolverBuilderBeanCriteria> resolverBuilders;

        SpqrBean(ApplicationContext context, String beanName, AnnotatedType type, List<ResolverBuilderBeanCriteria> resolverBuilders) {
            BeanScope beanScope = BeanScope.findBeanScope(context, beanName);
            if (beanScope == BeanScope.SINGLETON) {
                Object bean = context.getBean(beanName);
                this.beanSupplier = () -> bean;
            } else {
                this.beanSupplier = () -> context.getBean(beanName);
            }
            this.scope = beanScope;
            this.type = type;
            this.exposedType = context.getType(beanName);
            this.resolverBuilders = Collections.unmodifiableList(resolverBuilders);
        }
    }

    private static class ResolverBuilderBeanCriteria {
        private final Class<? extends ResolverBuilder> resolverType;
        private final String value;
        private final Class<? extends Annotation> qualifierType;

        private ResolverBuilderBeanCriteria(Class<? extends ResolverBuilder> resolverType, String value, Class<? extends Annotation> qualifierType) {
            this.resolverType = resolverType;
            this.value = value;
            this.qualifierType = qualifierType;
        }

        String getValue() {
            return value;
        }

        Class<? extends Annotation> getQualifierType() {
            return qualifierType;
        }

        Class<? extends ResolverBuilder> getResolverType() {
            return resolverType;
        }
    }

    private enum BeanScope {
        SINGLETON,
        PROTOTYPE,
        UNKNOWN;

        static BeanScope findBeanScope(ApplicationContext context, String beanName) {
            if (context.isSingleton(beanName)) {
                return SINGLETON;
            } else if (context.isPrototype(beanName)) {
                return PROTOTYPE;
            } else {
                return UNKNOWN;
            }
        }
    }

}
