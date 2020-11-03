# graphql-spqr-spring-boot-starter
Spring Boot 2 starter powered by GraphQL SPQR

[![Join the chat at https://gitter.im/leangen/graphql-spqr](https://badges.gitter.im/leangen/graphql-spqr.svg)](https://gitter.im/leangen/graphql-spqr?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.leangen.graphql/graphql-spqr-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.leangen.graphql/graphql-spqr-spring-boot-starter)
[![Javadoc](http://javadoc-badge.appspot.com/io.leangen.graphql/graphql-spqr-spring-boot-starter.svg?label=javadoc)](http://www.javadoc.io/doc/io.leangen.graphql/graphql-spqr-spring-boot-starter)
[![Build Status](https://travis-ci.org/leangen/graphql-spqr-spring-boot-starter.svg?branch=master)](https://travis-ci.org/leangen/graphql-spqr-spring-boot-starter)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)](https://raw.githubusercontent.com/leangen/graphql-spqr-spring-boot-starter/master/LICENSE)

## NOTICE
This project is on a temporary hiatus. I hope to resume it shortly. In doing so, I must first assess the current state. I have removed the access to issues for the time being to get a chance to clean them up and prevent people from abusing the issues to ask for new releases. I'll restore the access as soon as I can. Sorry for the inconvenience, and thanks for your patience.


## A friendly warning

This project is still in early development and, while fairly well tested, should be considered as ALPHA stage as long as the version is 0.0.X.

## Project setup / Dependencies

To use this starter in a typical Spring Boot project, add the following dependencies to your project:

```xml

<dependencies>
  <dependency>
    <groupId>io.leangen.graphql</groupId>
    <artifactId>graphql-spqr-spring-boot-starter</artifactId>
    <version>0.0.4</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
</dependencies>
```
[There's also a very basic sample project](https://github.com/leangen/graphql-spqr-samples/tree/master/spring-boot-starter-sample) 

## Defining the operation sources (the beans that get exposed via the API)

All beans in Spring's application context annotated with `@GraphqlApi` are considered to be operation sources (a concept similar to `Controller` beans in Spring MVC).
This annotation can be used in combination with `@Component/@Service/@Repository` or `@Bean` annotations, e.g.

```java
    @Component
    @GraphQLApi
    private class UserService {
        //Query/mutation/subscription methods
        ...
    }
```
or
```java
    @Bean
    @GraphQLApi
    public userService() {
        return new UserService(...);
    }
```

## Choosing which methods get exposed through the API

To deduce which methods of each operation source class should be exposed as GraphQL queries/mutations/subscriptions, SPQR uses the concept of a `ResolverBuilder` (since each exposed method acts as a resolver function for a GraphQL operation).
To cover the basic approaches `SpqrAutoConfiguration` registers a bean for each of the three built-in `ResolverBuilder` implementations:

* `AnnotatedResolverBuilder` - exposes only the methods annotated by `@GraphQLQuery`, `@GraphQLMutation` or `@GraphQLSubscription`
* `PublicResolverBuilder` - exposes all `public` methods from the operations source class (methods returning `void` are considered mutations)
* `BeanResolverBuilder` - exposes all getters as queries and setters as mutations (getters returning `Publisher<T>` are considered subscriptions)

It is also possible to implement custom resolver builders by implementing the `ResolverBuilder` interface.

Resolver builders can be declared both globally and on the operation source level. If not sticking to the defaults, it is generally safer to explicitly customize on the operation source level, unless the rules are absolutely uniform across all operation sources.
Customizing on both levels simultaneously will work but could prove tricky to control as your API grows.

At the moment SPQR's (v0.10.1) default resolver builder is `AnnotatedResolverBuilder`. This starter follows that convention and will continue to do so if at some point SPQR's default changes.

### Customizing resolver builders globally

To change the default resolver builders globally, implement and register a bean of type `ExtensionProvider<ResolverBuilder>`.
A simplified example of this could be:

```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> resolverBuilderExtensionProvider() {
        return (config, current) -> {
            List<ResolverBuilder> resolverBuilders = new ArrayList<>();

            //add a custom subtype of PublicResolverBuilder that only exposes a method if it's called "greeting"
            resolverBuilders.add(new PublicResolverBuilder() {
                @Override
                protected boolean isQuery(Method method) {
                    return super.isQuery(method) && method.getName().equals("greeting");
                }
            });
            //add the default builder
            resolverBuilders.add(new AnnotatedResolverBuilder());

            return resolverBuilders;
        };
    }
```
This would add two resolver builders that apply to _all_ operation sources.
The First one exposes all public methods named _greeting_. The second is the inbuilt `AnnotatedResolverBuilder` (that exposes only the explicitly annotated methods).
A quicker way to achieve the same would be:

```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> resolverBuilderExtensionProvider() {
        //prepend the custom builder to the provided list of defaults
        return (config, current) -> current.prepend(new PublicResolverBuilder() {
                @Override
                protected boolean isQuery(Method method) {
                    return super.isQuery(method) && method.getName().equals("greeting");
                }
            });
    };
```

### Customizing the resolver builders for a specific operation source

To attach a resolver builder to a specific source (bean), use the `@WithResolverBuilder` annotation on it.
This annotation also works both on the beans registered by `@Component/@Service/@Repository` or `@Bean` annotations.

As an example, we can expose the `greeting` query by using:

```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class) //exposes all getters
    private class MyOperationSource {
        public String getGreeting(){
            return "Hello world !";
        }
    }
```

or:

```java
    @Bean
    @GraphQLApi
    //No explicit resolver builders declared, so AnnotatedResolverBuilder is used
    public MyOperationSource() {
        @GraphQLQuery(name = "greeting")
        public String getGreeting() {
            return "Hello world !";
        }
    }
``` 

It is also entirely possible to use more than one resolver builder on the same operation source e.g.

```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    @WithResolverBuilder(AnnotatedResolverBuilder.class)
    private class MyOperationSource {
        //Exposed by BeanResolverBuilder because it's a getter
        public String getGreeting(){
            return "Hello world !";
        }

        //Exposed by AnnotatedResolverBuilder because it's annotated
        @GraphQLQuery
        public String personalGreeting(String name){
            return "Hello " + name + " !"; 
        }
    }
```
This way, both queries are exposed but in different ways. The same would work on a bean registered using the `@Bean` annotation.

## Customize GraphQL type information

Sometimes it is useful to have an automated strategy for generating type names, descriptions and order of fields within the type.
To do this SPQR uses `TypeInfoGenerator` on a global level. When using this starter the most convenient way is to wire a single bean of that type in the application context.

```java
    @Bean
    public TypeInfoGenerator testTypeInfoGenerator() {
        return new TypeInfoGenerator() {
            @Override
            public String generateTypeName(AnnotatedType type, MessageBundle messageBundle) {
                return nameGenerationMethodLocalized(type, messageBundle);
            }

            @Override
            public String generateTypeDescription(AnnotatedType type, MessageBundle messageBundle) {
                return descriptionGenerationMethodLocalized(type, messageBundle);
            }

            @Override
            public String[] getFieldOrder(AnnotatedType type, MessageBundle messageBundle) {
                return fieldOrderGenerationMethodLocalized(type, messageBundle);
            }

        };
    }
```

## Advanced config

### Available Properties

| Property | Default Value |
| ------ | ------ |
| graphql.spqr.base-packages | n/a |
| graphql.spqr.abstract-input-type-resolution | false |
| graphql.spqr.relay.enabled | false |
| graphql.spqr.relay.mutation-wrapper | n/a |
| graphql.spqr.relay.mutation-wrapper-description | n/a |
| graphql.spqr.relay.connection-check-relaxed | false |
| graphql.spqr.relay.spring-data-compatible | false |
| graphql.spqr.http.enabled | true |
| graphql.spqr.http.endpoint | /graphql |
| graphql.spqr.ws.enabled | true |
| graphql.spqr.ws.endpoint | n/a |
| graphql.spqr.ws.allowedOrigins | * |
| graphql.spqr.ws.keepAlive.enabled | false |
| graphql.spqr.ws.keepAlive.intervalMillis | 10000 |
| graphql.spqr.gui.enabled | true |
| graphql.spqr.gui.endpoint | /gui |
| graphql.spqr.gui.targetEndpoint | n/a |
| graphql.spqr.gui.targetWsEndpoint | n/a |
| graphql.spqr.gui.pageTitle | GraphQL Playground |

### Customize mapping of GraphQL values to Java values

Object in charge of doing this in SPQR is `ValueMapperFactory`. Again the simplest way to make use of this when using the starter is to wire a single bean of this type into the application context.

```java
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
                return null;
            }
        };
    }
``` 
NOTE: SPQR comes with `JacksonValueMapper` and `GsonValueMapperFactory` so in reality this should be rarely needed as these are by far the most frequently used libraries in Java.

### Customizing input and output converters

Analogous to the rest of the configuration, single beans should be wired into the context. As this is done in functional style in SPQR it is not possible to set chains of `InputConverter` and `OutputConverter`, but by passing a lambda that will manipulate the chains.

Extension provider for input converters
```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, InputConverter> testInputConverterExtensionProvider() {
        return (config, current) -> current.prepend( //Insert before the defaults. Or return a new list to take full control.
            new InputConverter() {
                @Override
                public Object convertInput(Object substitute, AnnotatedType type, GlobalEnvironment environment, ValueMapper valueMapper) {
                    return ...;
                }

                @Override
                public boolean supports(AnnotatedType type) {
                    return ...;
                }

                @Override
                public AnnotatedType getSubstituteType(AnnotatedType original) {
                    return ...;
                }
            }
        );
    }
```

Extension provider for output converters
```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, OutputConverter> testOutputConverterExtensionProvider() {
         //Insert a custom converter after the built-in IdAdapter (which is generally a safe position).
         //Return a new list instead to take full control. 
        return (config, current) -> current.insertAfter(IdAdapter.class,
            new OutputConverter() {
                @Override
                public Object convertOutput(Object original, AnnotatedType type, ResolutionEnvironment resolutionEnvironment) {
                    return ...;
                }

                @Override
                public boolean supports(AnnotatedType type) {
                    return ...;
                }
            }
         );
    }
```
### Custom type mapper for GraphQL output and input types

Again wire a single bean of type `ExtensionProvider<GeneratorConfiguration, TypeMapper>` into the application context to manipulate mapper chain.

```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, TypeMapper> customTypeMappers() {
    //Insert a custom mapper after the built-in IdAdapter (which is generally a safe position)
    return (config, current) -> current.insertAfter(IdAdapter.class,
            new TypeMapper() {
                @Override
                public GraphQLOutputType toGraphQLType(AnnotatedType javaType, OperationMapper operationMapper, Set<Class<? extends TypeMapper>> mappersToSkip, BuildContext buildContext) {
                    return new GraphQLOutputType() {
                        @Override
                        public String getName() {
                            return ...;
                        }
                    };
                }

                @Override
                public GraphQLInputType toGraphQLInputType(AnnotatedType javaType, OperationMapper operationMapper, Set<Class<? extends TypeMapper>> mappersToSkip, BuildContext buildContext) {
                    return new GraphQLInputType() {
                        @Override
                        public String getName() {
                            return ...;
                        }
                    };
                }

                @Override
                public boolean supports(AnnotatedType type) {
                    return ...;
                }
            }
        );
    }

```

### Custom argument injector

Also has a functional API, utilised by wiring a single bean of type `ExtensionProvider<GeneratorConfiguration, ArgumentInjector>`.

```java
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ArgumentInjector> testArgumentInjectorExtensionProvider() {
        return (config, current) -> current.prepend(
           new ArgumentInjector() {
               @Override
               public Object getArgumentValue(ArgumentInjectorParams params) {
                   return ...;
               }

               @Override
               public boolean supports(AnnotatedType type, Parameter parameter) {
                   return ...;
               }

           }
        );
    }
```

### Custom input fields

Wiring a single bean of type `ExtensionProvider<GraphQLSchemaGenerator.ExtendedConfiguration, InputFieldBuilder>` will allow you to manipulate the input builder chain.

```java
    @Bean
    public ExtensionProvider<ExtendedGeneratorConfiguration, InputFieldBuilder> testInputFieldBuilder() {
        return (config, current) -> current.prepend( //Prepend your custom builder so it goes before the built-in ones
                new InputFieldBuilder() {
                    @Override
                    public Set<InputField> getInputFields(InputFieldBuilderParams params) {
                        return ...; //Build the input fields for the given type
                    }

                    @Override
                    public boolean supports(AnnotatedType type) {
                        return ...; //Does this builder support the given type?
                    }
                });
    }
```

NOTE: In SPQR `InputFieldBuilder` is already implemented by `JacksonValueMapper` and `GsonValueMapper`.


### More to follow soon ...
