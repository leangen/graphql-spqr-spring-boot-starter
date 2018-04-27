# graphql-spqr-spring-boot-starter
Spring Boot 2 starter powered by GraphQL SPQR

[![Join the chat at https://gitter.im/leangen/graphql-spqr](https://badges.gitter.im/leangen/graphql-spqr.svg)](https://gitter.im/leangen/graphql-spqr?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.leangen.graphql/spqr/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.leangen.graphql/graphql-spqr-spring-boot-starter)
[![Javadoc](http://javadoc-badge.appspot.com/io.leangen.graphql/spqr.svg?label=javadoc)](http://www.javadoc.io/doc/io.leangen.graphql/graphql-spqr-spring-boot-starter)
[![Build Status](https://travis-ci.org/leangen/graphql-spqr.svg?branch=master)](https://travis-ci.org/leangen/graphql-spqr-spring-boot-starter)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)](https://raw.githubusercontent.com/leangen/graphql-spqr-spring-boot-starter/master/LICENSE)
[![Semver](http://img.shields.io/SemVer/2.0.0.png)](http://semver.org/spec/v2.0.0.html)

## A friendly warning

This project is still in early development and, while fairly well tested, should be considered as ALPHA stage as long as the version is 0.0.X.

## Dependencies

If you want to use this starter in your project you'll need the following dependencies on your classpath in a typical spring boot project.

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>2.0.1.RELEASE</version>
</parent>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
  </dependency>
  <dependency>
    <groupId>io.leangen.graphql</groupId>
    <artifactId>graphiql-spring-boot-starter</artifactId>
    <version>0.0.1</version>
  </dependency>
  <dependency>
    <groupId>io.leangen.graphql</groupId>
    <artifactId>spqr</artifactId>
    <version>0.9.7</version>
  </dependency>
</dependencies>
```
Of course analogous for Gradle project.

The plan is that in 0.0.2 alpha release `spring-boot-starter-websocket` dependency won't be necessary if you're not using autoconfig for graphql subscriptions.

## Defining operation sources for the API

All beans in spring context annotated with `@GraphqlApi` are considered to be operation sources (concept similar to `Controller` class in SpringMVC).

This annotation can be used in combination with `@Context` or `@Bean` annotations, e.g.

```java
    @Component
    @GraphQLApi
    private class MyOperationSource {
        ...
    }
```
or 
```java
    @Bean
    @GraphQLApi
    public MyOperationSource() {
        ...
    }
``` 

### Which methods of operation source get exposed through the API
To deduce which methods of the operation source class will be exposed as a query or a mutation SPQR uses a concept of a `ResolverBuilder`. To cover the basic approaches `SpqrAutoConfiguration` will add beans for all three basic resolver builder implementations.
* `AnnotatedResolverBuilder` which detects usage of annotations from `io.leangen.graphql.annotations` package to decide if a method should be exposed through GraphQL API
* `PublicResolverBuilder` which exposes all `public` methods from the operations source class
* `BeanResolverBuilder` which exposes all getters as queries and setters as mutations

It is also possible to implement custom resolver builders by implementing the `ResolverBuilder` interface.

Resolver builders can be declared on both global and operation source specific level. Generally we consider it a better idea to declare explicitly on operation source level unless rules are absolutely the same on all operation sources. Mixing will work but could prove tricky to controll as your API grows.

At the moment SPQR's (v0.9.7) default resolver builder is `AnnotatedResolverBuilder`, this starter follows that convention and will continue to do so if at some point SPQR's default changes.

#### Global resolver builder configuration

To change the global default configuration you need to implement a bean of type `ExtensionProvider<ResolverBuilder>` and add it to the application context.
A simplified example of this could be:
```java
    @Bean
    public ExtensionProvider<ResolverBuilder> resolverBuilderExtensionProvider() {
        return (config, defaults) -> {
            List<ResolverBuilder> resolverBuilders = new ArrayList<>();

            resolverBuilders.add(new PublicResolverBuilder() {
                @Override
                protected boolean isQuery(Method method) {
                    return super.isQuery(method) && method.getName().equals("greeting");
                }
            });

            resolverBuilders.add(new AnnotatedResolverBuilder());

            return resolverBuilders;
        };
    }
```
This would add two resolver builders that would apply to all operation sources.
First one would expose all public methods named 'greeting'. The second is the inbuilt `AnnotatedResolverBuilder`.
There are of course nicer ways to write this but the idea is to keep the example as clear as possible.

#### Operation source specific configuration

To leverage the underlying SPQR features `graphiql-spring-boot-starter` uses `@WithResolverBuilder` annotation on the operation source bean.

This annotation can also be used in combination with both `@Context` or `@Bean` annotations as used normally in spring framework.

As an example we can expose a `getGreetingFromAnnotatedSource_wiredAsComponent` query by using:

```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    private class MyOperationSource {
        public String getGreetingFromAnnotatedSource_wiredAsComponent(){
            return "Hello world !"; 
        }
    }

```

or `greetingFromAnnotatedSource_wiredAsBean` query:
```java
    @Bean
    @GraphQLApi
    public MyOperationSource() {
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsBean")
        public String getGreeting(){
            return "Hello world !"; 
        }
    }
``` 

Note that if no explicit resolver builders are declared `AnnotatedResolverBuilder` will be used as default.

Also it is completely normal to use more than one resolver builder on the same operation source e.g.
```java
    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    @WithResolverBuilder(AnnotatedResolverBuilder.class)
    private class MyOperationSource {
        public String getGreetingFromAnnotatedSource_wiredAsComponent_withBeanResolverBuilder(){
            return "Hello world !"; 
        }
        
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsBean_withAnnotatedResolverBuildr")
        public String getGreeting(){
            return "Hello world !"; 
        }
    }
```
this way we would expose both queries discovered in a different way. And same would also work if we were using it with `@Bean` annotation.

### More to follow soon ...