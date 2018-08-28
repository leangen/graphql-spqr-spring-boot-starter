package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.extension.ExtensionProvider;
import io.leangen.graphql.generator.mapping.ArgumentInjector;
import io.leangen.graphql.generator.mapping.InputConverter;
import io.leangen.graphql.generator.mapping.OutputConverter;
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
import io.leangen.graphql.spqr.spring.annotation.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotation.WithResolverBuilder;
import io.leangen.graphql.spqr.spring.annotation.WithResolverBuilders;
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
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Configuration
@ConditionalOnClass(GraphQLSchemaGenerator.class)
@EnableConfigurationProperties(SpqrProperties.class)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class SpqrAutoConfiguration {

    private final ConfigurableApplicationContext context;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, ResolverBuilder> globalResolverBuilderExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, TypeMapper> typeMapperExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, InputConverter> inputConverterExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, OutputConverter> outputConverterExtensionProvider;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, ArgumentInjector> argumentInjectorExtensionProvider;

    @Autowired(required = false)
    private ValueMapperFactory valueMapperFactory;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.ExtendedConfiguration, InputFieldBuilder> inputFieldBuilderProvider;

    @Autowired(required = false)
    private ExtensionProvider<GraphQLSchemaGenerator.Configuration, ResolverBuilder> nestedResolverBuilders;

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

    @Autowired
    public SpqrAutoConfiguration(ConfigurableApplicationContext context) {
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

        schemaGenerator.withBasePackages(spqrProperties.getQueryBasePackages());

        if (spqrProperties.getRelaySupported()) {
            if (!StringUtils.isEmpty(spqrProperties.getRelayMutationWrapper())) {
                schemaGenerator.withRelayCompliantMutations(
                        spqrProperties.getRelayMutationWrapper(), spqrProperties.getRelayMutationWrapperDescription()
                );
            } else {
                schemaGenerator.withRelayCompliantMutations();
            }
        }

        Map<String, SpqrBean> beansWiredAsComponent = findGraphQLApiComponents();
        addOperationSources(schemaGenerator, beansWiredAsComponent.values());

        List<SpqrBean> beansWiredWithAsBeans = findGraphQLApiBeans();
        addOperationSources(schemaGenerator, beansWiredWithAsBeans);

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

        if (valueMapperFactory != null) {
            schemaGenerator.withValueMapperFactory(valueMapperFactory);
        }

        if (inputFieldBuilderProvider != null) {
            schemaGenerator.withInputFieldBuilders(inputFieldBuilderProvider);
        }

        if (typeInfoGenerator != null) {
            schemaGenerator.withTypeInfoGenerator(typeInfoGenerator);
        }

        if (nestedResolverBuilders != null) {
            schemaGenerator.withNestedResolverBuilders(nestedResolverBuilders);
        }

        if (spqrProperties.getAbstractInputTypeResolution()) {
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

        if (spqrProperties.getRelayConnectionCheckRelaxed()) {
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
                            spqrBean.resolverBuilders.stream()
                                    .map(resolverBuilderBeanIdentity -> findQualifiedBeanByType(resolverBuilderBeanIdentity.getResolverType(),
                                            resolverBuilderBeanIdentity.getValue(),
                                            resolverBuilderBeanIdentity.getQualifierType()))
                                    .toArray(ResolverBuilder[]::new)
                        )
                );
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchema graphQLSchema(GraphQLSchemaGenerator schemaGenerator) {
        return schemaGenerator.generate();
    }

    private <T> T findQualifiedBeanByType(Class<? extends T> type, String qualifierValue, Class<? extends Annotation> qualifierType) {
        final NoSuchBeanDefinitionException noSuchBeanDefinitionException = new NoSuchBeanDefinitionException(qualifierValue, "No matching " + type.getSimpleName() +
                " bean found for qualifier " + qualifierValue + " of type " + qualifierType.getSimpleName() +" !");
        try {

            if (StringUtils.isEmpty(qualifierValue)) {
                if (qualifierType.equals(Qualifier.class)){
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

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private List<SpqrBean> findGraphQLApiBeans() {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();

        List<SpqrBean> spqrBeans = new ArrayList<>();

        for (String beanName : factory.getBeanDefinitionNames()) {
            BeanDefinition bd = factory.getBeanDefinition(beanName);

            if(bd.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                Map<String, Object> attributes = metadata.getAnnotationAttributes(GraphQLApi.class.getName());
                if (null == attributes) {
                    continue;
                }

                SpqrBean spqrBean = new SpqrBean(context, beanName);

                Map<String, Object> withResolverBuildersAttributes = metadata.getAnnotationAttributes(WithResolverBuilders.class.getTypeName());
                if (withResolverBuildersAttributes != null) {
                    AnnotationAttributes[] annotationAttributesArray = (AnnotationAttributes[]) withResolverBuildersAttributes.get("value");
                    Arrays.stream(annotationAttributesArray)
                            .forEach(annotationAttributes ->
                                    spqrBean.getResolverBuilders().add(
                                            new ResolverBuilderBeanIdentity(
                                                    (Class<? extends ResolverBuilder>) annotationAttributes.get("value"),
                                                    (String) annotationAttributes.get("qualifierValue"),
                                                    (Class<? extends Annotation>) annotationAttributes.get("qualifierType"))
                                    )
                            );
                } else {
                    Map<String, Object> withResolverBuilderAttributes = metadata.getAnnotationAttributes(WithResolverBuilder.class.getTypeName());
                    if (withResolverBuilderAttributes != null) {
                        spqrBean.getResolverBuilders().add(
                                new ResolverBuilderBeanIdentity(
                                        (Class<? extends ResolverBuilder>) withResolverBuilderAttributes.get("value"),
                                        (String) withResolverBuilderAttributes.get("qualifierValue"),
                                        (Class<? extends Annotation>) withResolverBuilderAttributes.get("qualifierType"))
                        );
                    }
                }

                spqrBeans.add(spqrBean);
            }
        }

        return spqrBeans;
    }

    private Map<String, SpqrBean> findGraphQLApiComponents() {
        final Map<String, Object> operationSourcesBeans = context.getBeansWithAnnotation(GraphQLApi.class);

        Map<String, SpqrBean> result = new HashMap<>();
        for (String beanName : operationSourcesBeans.keySet()) {
            result.put(beanName, new SpqrBean(context,beanName));

            Class<?> operationSourceBeanClass = operationSourcesBeans.get(beanName).getClass();
            if (operationSourceBeanClass.isAnnotationPresent(WithResolverBuilder.class)) {
                WithResolverBuilder withResolverBuilder = operationSourceBeanClass.getAnnotation(WithResolverBuilder.class);
                result.get(beanName).resolverBuilders.add(new ResolverBuilderBeanIdentity(withResolverBuilder.value(), withResolverBuilder.qualifierValue(), withResolverBuilder.qualifierType()));
            } else if (operationSourceBeanClass.isAnnotationPresent(WithResolverBuilders.class)) {
                for (WithResolverBuilder withResolverBuilder : operationSourceBeanClass.getAnnotation(WithResolverBuilders.class).value()) {
                    result.get(beanName).resolverBuilders.add(new ResolverBuilderBeanIdentity(withResolverBuilder.value(), withResolverBuilder.qualifierValue(), withResolverBuilder.qualifierType()));
                }

            }
        }
        return result;
    }

    private class SpqrBean {
        private final BeanScope scope;
        private final Object springBean;
        private final List<ResolverBuilderBeanIdentity> resolverBuilders;

        SpqrBean(ApplicationContext context, String beanName) {
            this.springBean = context.getBean(beanName);
            this.scope = BeanScope.findBeanScope(context, beanName);
            this.resolverBuilders  = new ArrayList<>();
        }

        BeanScope getScope() {
            return scope;
        }

        Object getSpringBean() {
            return springBean;
        }

        List<ResolverBuilderBeanIdentity> getResolverBuilders() {
            return resolverBuilders;
        }
    }

    private class ResolverBuilderBeanIdentity {
        private final Class<? extends ResolverBuilder> resolverType;
        private final String value;
        private final Class<? extends Annotation> qualifierType;

        private ResolverBuilderBeanIdentity(Class<? extends ResolverBuilder> resolverType, String value, Class<? extends Annotation> qualifierType) {
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

    private enum  BeanScope {
        SINGLETON,
        PROTOTYPE;

        static BeanScope findBeanScope(ApplicationContext context, String beanName) {
            if (context.isSingleton(beanName)) {
                return BeanScope.SINGLETON;
            } else if (context.isPrototype(beanName)) {
                return BeanScope.PROTOTYPE;
            } else {
                //TODO log warning and proceed
                throw new RuntimeException("Unsupported bean scope");
            }
        }
    }

}
