package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.extension.ExtensionProvider;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.OperationMapper;
import io.leangen.graphql.generator.mapping.ArgumentInjector;
import io.leangen.graphql.generator.mapping.ArgumentInjectorParams;
import io.leangen.graphql.generator.mapping.InputConverter;
import io.leangen.graphql.generator.mapping.OutputConverter;
import io.leangen.graphql.generator.mapping.TypeMapper;
import io.leangen.graphql.metadata.InputField;
import io.leangen.graphql.metadata.messages.MessageBundle;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.metadata.strategy.type.TypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.type.TypeTransformer;
//import io.leangen.graphql.metadata.strategy.value.InputFieldDiscoveryStrategy;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.spqr.spring.annotation.GraphQLApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpqrAutoConfiguration.class, GlobalConfig_SpqrAutoConfigurationTest.TypeMapper_TestConfig.class})
@TestPropertySource(locations = "classpath:application.properties")
public class GlobalConfig_SpqrAutoConfigurationTest {

    @Autowired
    private SpqrProperties spqrProperties;

    @Autowired
    private GraphQLSchemaGenerator schemaGenerator;

    @Test
    public void propertiesLoad() {
        Assert.assertNotNull(spqrProperties);
        Assert.assertNotNull(spqrProperties.getQueryBasePackages());
        Assert.assertEquals(1, spqrProperties.getQueryBasePackages().length);
        Assert.assertEquals("com.bogus.package", spqrProperties.getQueryBasePackages()[0]);
    }

    @Test
    public void ResolverBuilderExtensionProvider_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        List<ExtensionProvider<GraphQLSchemaGenerator.Configuration, ResolverBuilder>> resolverBuilderProviders =
                getPrivateFieldValueFromObject(schemaGenerator, "resolverBuilderProviders");

        Assert.assertNotNull(resolverBuilderProviders);

        Assert.assertEquals(1, resolverBuilderProviders.size());

        ExtensionProvider<GraphQLSchemaGenerator.Configuration, ResolverBuilder> resolverBuilderExtensionProvider = resolverBuilderProviders.iterator().next();

        Assert.assertNotNull(resolverBuilderExtensionProvider);

        List<ResolverBuilder> resolverBuilders = resolverBuilderExtensionProvider.getExtensions(null, null);

        Assert.assertNotNull(resolverBuilders);
        Assert.assertEquals(2, resolverBuilders.size());

    }

    @Test
    public void typeInfoGenerator_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        TypeInfoGenerator typeInfoGenerator = getPrivateFieldValueFromObject(schemaGenerator, "typeInfoGenerator");

        Assert.assertNotNull(typeInfoGenerator);

        Assert.assertEquals("OK", typeInfoGenerator.generateTypeName(null, null));
        Assert.assertEquals("OK", typeInfoGenerator.generateTypeDescription(null, null));
        Assert.assertEquals(1, typeInfoGenerator.getFieldOrder(null, null).length);
    }

//    @Test
//    public void inputFieldDiscoveryStrategy_schemaGeneratorConfigTest() {
//        Assert.assertNotNull(schemaGenerator);
//
//        InputFieldDiscoveryStrategy inputFieldDiscoveryStrategy =
//                getPrivateFieldValueFromObject(schemaGenerator, "inputFieldStrategy");
//
//        Assert.assertNotNull(inputFieldDiscoveryStrategy);
//
//        Set<InputField> inputFields = inputFieldDiscoveryStrategy.getInputFields(null, null, null);
//
//        Assert.assertFalse(inputFields.isEmpty());
//        Assert.assertEquals(1, inputFields.size());
//
//        InputField inputField = inputFields.iterator().next();
//
//        Assert.assertNotNull(inputField);
//        Assert.assertEquals("OK", inputField.getName());
//        Assert.assertEquals("OK", inputField.getDescription());
//        Assert.assertEquals("java.lang.Object", inputField.getJavaType().getType().getTypeName());
//    }

//    @Test
//    public void valueMapperFactory_schemaGeneratorConfigTest() {
//        Assert.assertNotNull(schemaGenerator);
//
//        ValueMapperFactory valueMapperFactory =
//                getPrivateFieldValueFromObject(schemaGenerator, "valueMapperFactory");
//
//        Assert.assertNotNull(valueMapperFactory);
//
//        ValueMapper valueMapper = valueMapperFactory.getValueMapper();
//
//        Assert.assertNotNull(valueMapper);
//
//        Assert.assertNull(valueMapper.fromString("test!@#$%^&*", String.class.getAnnotatedSuperclass()));
//        Assert.assertEquals("OK", valueMapper.toString("test!@#$%^&*"));
//    }

    @Test
    public void argumentInjectorExtensionProvider_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        List<ExtensionProvider<GraphQLSchemaGenerator.Configuration, ArgumentInjector>> argumentInjectorProviders =
                getPrivateFieldValueFromObject(schemaGenerator, "argumentInjectorProviders");

        Assert.assertNotNull(argumentInjectorProviders);

        Assert.assertEquals(1, argumentInjectorProviders.size());

        ExtensionProvider<GraphQLSchemaGenerator.Configuration, ArgumentInjector> argumentInjectorExtensionProvider = argumentInjectorProviders.iterator().next();

        Assert.assertNotNull(argumentInjectorExtensionProvider);

        List<ArgumentInjector> argumentInjectors = argumentInjectorExtensionProvider.getExtensions(null, null);

        Assert.assertNotNull(argumentInjectors);
        Assert.assertEquals(1, argumentInjectors.size());

        ArgumentInjector argumentInjector = argumentInjectors.iterator().next();

        Assert.assertNotNull(argumentInjector);
        Assert.assertEquals("OK argument injector", argumentInjector.getArgumentValue(null));

    }

    @Test
    public void outputConverterExtensionProvider_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        List<ExtensionProvider<GraphQLSchemaGenerator.Configuration, OutputConverter>> outputConverterProviders =
                getPrivateFieldValueFromObject(schemaGenerator, "outputConverterProviders");

        Assert.assertNotNull(outputConverterProviders);

        Assert.assertEquals(1, outputConverterProviders.size());

        ExtensionProvider<GraphQLSchemaGenerator.Configuration, OutputConverter> outputConverterExtensionProvider = outputConverterProviders.iterator().next();

        Assert.assertNotNull(outputConverterExtensionProvider);

        List<OutputConverter> outputConverters = outputConverterExtensionProvider.getExtensions(null, null);

        Assert.assertNotNull(outputConverters);
        Assert.assertEquals(1, outputConverters.size());

        OutputConverter<?, ?> outputConverter = outputConverters.iterator().next();

        Assert.assertNotNull(outputConverter);
        Assert.assertEquals("OK output converter", outputConverter.convertOutput(null, null, null));
    }

    @Test
    public void inputConverterExtensionProvider_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        List<ExtensionProvider<GraphQLSchemaGenerator.Configuration, InputConverter>> inputConverterProviders =
                getPrivateFieldValueFromObject(schemaGenerator, "inputConverterProviders");

        Assert.assertNotNull(inputConverterProviders);

        Assert.assertEquals(1, inputConverterProviders.size());

        ExtensionProvider<GraphQLSchemaGenerator.Configuration, InputConverter> inputConverterExtensionProvider = inputConverterProviders.iterator().next();

        Assert.assertNotNull(inputConverterExtensionProvider);

        List<InputConverter> inputConverters = inputConverterExtensionProvider.getExtensions(null, null);

        Assert.assertNotNull(inputConverters);
        Assert.assertEquals(1, inputConverters.size());

        InputConverter<?, ?> inputConverter = inputConverters.iterator().next();

        Assert.assertNotNull(inputConverter);
        Assert.assertEquals("OK input converter", inputConverter.convertInput(null, null, null, null));
    }

    @Test
    public void typeMapperExtensionProvider_schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);

        List<ExtensionProvider<GraphQLSchemaGenerator.Configuration, TypeMapper>> typeMapperExtensionProviders =
                getPrivateFieldValueFromObject(schemaGenerator, "typeMapperProviders");

        Assert.assertNotNull(typeMapperExtensionProviders);

        Assert.assertEquals(1, typeMapperExtensionProviders.size());

        ExtensionProvider<GraphQLSchemaGenerator.Configuration, TypeMapper> typeMapperExtensionProvider = typeMapperExtensionProviders.iterator().next();

        Assert.assertNotNull(typeMapperExtensionProvider);

        List<TypeMapper> typeMappers = typeMapperExtensionProvider.getExtensions(null, null);

        Assert.assertNotNull(typeMappers);
        Assert.assertEquals(1, typeMappers.size());

        TypeMapper typeMapper = typeMappers.iterator().next();

        Assert.assertNotNull(typeMapper);
        Assert.assertEquals("OK output type", typeMapper.toGraphQLType(null, null, null, null).getName());
        Assert.assertEquals("OK input type", typeMapper.toGraphQLInputType(null, null, null, null).getName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateFieldValueFromObject(Object object, String fieldName){
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return  (T) f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error getting private field value", e);
        }
    }


    @Configuration
    static class TypeMapper_TestConfig {
        @Component("annotatedOperationSourceBean")
        @GraphQLApi
        private class AnnotatedOperationSourceBean {
            @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsComponent")
            public String getGreeting(){
                return "Hello world !";
            }
        }

        @Bean
        public ExtensionProvider<GraphQLSchemaGenerator.Configuration, ResolverBuilder> testResolverBuilderExtensionProvider() {
            return (config, defaults) -> {
                List<ResolverBuilder> resolverBuilders = new ArrayList<>();

                resolverBuilders.add(new PublicResolverBuilder() {
                    @Override
                    protected boolean isQuery(Method method) {
                        return super.isQuery(method) && method.getName().equals("getGreeting");
                    }
                });

                resolverBuilders.add(new AnnotatedResolverBuilder());

                return resolverBuilders;
            };
        }

        @Bean
        public TypeInfoGenerator testTypeInfoGenerator() {
            return new TypeInfoGenerator() {
                @Override
                public String generateTypeName(AnnotatedType type, MessageBundle messageBundle) {
                    return "OK";
                }

                @Override
                public String generateTypeDescription(AnnotatedType type, MessageBundle messageBundle) {
                    return "OK";
                }

                @Override
                public String[] getFieldOrder(AnnotatedType type, MessageBundle messageBundle) {
                    return new String[] {"OK"};
                }

            };
        }

//        @Bean
//        public InputFieldDiscoveryStrategy testInputFieldDiscoveryStrategy() {
//            return new InputFieldDiscoveryStrategy() {
//                @Override
//                public Set<InputField> getInputFields(AnnotatedType type, InclusionStrategy inclusionStrategy, TypeTransformer typeTransformer) {
//                    InputField testField = new InputField("OK", "OK", String.class.getAnnotatedSuperclass(), null);
//                    return Collections.singleton(testField);
//                }
//            };
//        }

        @Bean
        public ValueMapperFactory testValueMapperFactory() {
            return (abstractTypes, environment) -> new ValueMapper() {
                @Override
                public <T> T fromInput(Object graphQLInput, Type sourceType, AnnotatedType outputType) {
                    return null;
                }

                @Override
                public <T> T fromString(String json, AnnotatedType type) {
                    return null;
                }

                @Override
                public String toString(Object output) {
                    return "OK";
                }
            };
        }

        @Bean
        public ExtensionProvider<GraphQLSchemaGenerator.Configuration, ArgumentInjector> testArgumentInjectorExtensionProvider() {
            return (config, defaults) -> Collections.singletonList(
                    new ArgumentInjector() {
                        @Override
                        public Object getArgumentValue(ArgumentInjectorParams params) {
                            return "OK argument injector";
                        }

                        @Override
                        public boolean supports(AnnotatedType type, Parameter parameter) {
                            return false;
                        }

                    }
            );
        }

        @Bean
        public ExtensionProvider<GraphQLSchemaGenerator.Configuration, OutputConverter> testOutputConverterExtensionProvider() {
            return (config, defaults) -> Collections.singletonList(
                    new OutputConverter() {
                        @Override
                        public Object convertOutput(Object original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
                            return "OK output converter";
                        }

                        @Override
                        public boolean supports(AnnotatedType type) {
                            return false;
                        }
                    });
        }

        @Bean
        public ExtensionProvider<GraphQLSchemaGenerator.Configuration, InputConverter> testInputConverterExtensionProvider() {
            return (config, defaults) -> Collections.singletonList(
                    new InputConverter() {
                        @Override
                        public Object convertInput(Object substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
                            return "OK input converter";
                        }

                        @Override
                        public boolean supports(AnnotatedType type) {
                            return false;
                        }

                        @Override
                        public AnnotatedType getSubstituteType(AnnotatedType original) {
                            return null;
                        }
                    });
        }

        @Bean
        public ExtensionProvider<GraphQLSchemaGenerator.Configuration, TypeMapper> testTypeMapperExtensionProvider() {
            return (config, defaults) -> Collections.singletonList(
                    new TypeMapper() {
                        @Override
                        public GraphQLOutputType toGraphQLType(AnnotatedType javaType, OperationMapper operationMapper, Set<Class<? extends TypeMapper>> mappersToSkip, BuildContext buildContext) {
                            return new GraphQLOutputType() {
                                @Override
                                public String getName() {
                                    return "OK output type";
                                }
                            };
                        }

                        @Override
                        public GraphQLInputType toGraphQLInputType(AnnotatedType javaType, OperationMapper operationMapper, Set<Class<? extends TypeMapper>> mappersToSkip, BuildContext buildContext) {
                            return new GraphQLInputType() {
                                @Override
                                public String getName() {
                                    return "OK input type";
                                }
                            };
                        }

                        @Override
                        public boolean supports(AnnotatedType type) {
                            return false;
                        }
                    });
        }

        @Bean
        public GraphQLSchema graphQLSchema(GraphQLSchemaGenerator schemaGenerator) {
            //Suppressing schema generation with bogus test parameters
            return null;
        }
    }
}

