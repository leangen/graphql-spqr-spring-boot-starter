package io.leangen.graphql.spqr.spring.test;

import io.leangen.graphql.ExtensionProvider;
import io.leangen.graphql.GeneratorConfiguration;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.spqr.spring.annotation.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotation.WithResolverBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;

@Configuration
public class ResolverBuilder_TestConfig {
    //------------------------------------------------------------------------------------------
    //------------ Global resolver builder config ----------------------------------------------
    //------------------------------------------------------------------------------------------
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> globalResolverBuilderExtensionProvider() {
        return (config, defaults) -> defaults.insert(0, new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method) {
                final String[] expectedFragments = new String[] {
                        "byCustomGlobalResolverBuilder"};

                return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        });
    }

    //------------------------------------------------------------------------------------------
    //------------ Operation Sources -----------------------------------------------------------
    //------------------------------------------------------------------------------------------

    @Component("annotatedOperationSourceBean")
    @GraphQLApi
    private static class AnnotatedOperationSourceBean {
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsComponent")
        public String getGreeting(){
            return "Hello world !";
        }
    }

    @Bean
    @GraphQLApi
    public AnnotatedOperationSourceWired annotatedOperationSourceWired() {
        return new AnnotatedOperationSourceWired();
    }

    private static class AnnotatedOperationSourceWired {
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsBean")
        public String getGreeting(){
            return "Hello world !";
        }
    }

    @Bean
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testStringQualifiedCustomResolverBuilderBean")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testStringQualifiedCustomResolverBuilderComponent")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierType = TestBeanQualifier.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierType = TestComponentQualifier.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testNamedCustomResolverBuilderBean")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testNamedCustomResolverBuilderComponent")
    public BeanOperationSource beanOperationSource() {
        return new BeanOperationSource();
    }

    public static class BeanOperationSource {
        public String getGreetingFromBeanSource_wiredAsBean_byCustomGlobalResolverBuilder() {
            return "Hello world !";
        }

        public String getGreetingFromBeanSource_wiredAsBean_byMethodName() {
            return "Hello world !";
        }

        @GraphQLQuery(name = "greetingFromBeanSource_wiredAsBean_byAnnotation")
        public String getGreeting(){
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }
        public String greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }
    }

    @Component
    @GraphQLApi
    @WithResolverBuilder(BeanResolverBuilder.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testStringQualifiedCustomResolverBuilderBean")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testStringQualifiedCustomResolverBuilderComponent")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierType = TestBeanQualifier.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierType = TestComponentQualifier.class)
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testNamedCustomResolverBuilderBean")
    @WithResolverBuilder(value = ResolverBuilder.class, qualifierValue = "testNamedCustomResolverBuilderComponent")
    public static class ComponentOperationSource {
        public String getGreetingFromBeanSource_wiredAsComponent_byCustomGlobalResolverBuilder() {
            return "Hello world !";
        }

        public String getGreetingFromBeanSource_wiredAsComponent_byMethodName() {
            return "Hello world !";
        }

        @GraphQLQuery(name = "greetingFromBeanSource_wiredAsComponent_byAnnotation")
        public String getGreeting(){
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsBean() {
            return "Hello world !";
        }

        public String greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsComponent() {
            return "Hello world !";
        }

        @GraphQLQuery(name = "greetingFromBeanSource_integer")
        public Mono<Integer> getInteger() {
//            return Mono.just("PHP");
            return Mono.just(1984);
        }
    }


    //------------------------------------------------------------------------------
    //--------------------- ResolverBuilders ---------------------------------------
    //------------------------------------------------------------------------------

    @Bean
    @Qualifier("testStringQualifiedCustomResolverBuilderBean")
    public ResolverBuilder customStringQualifiedResolverBuilder() {
        return new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byStringQualified",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }

    @Bean
    @TestBeanQualifier
    public ResolverBuilder customAnnotationQualifiedResolverBuilder() {
        return new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byAnnotationQualified",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }

    @Bean(name = "testNamedCustomResolverBuilderBean")
    public ResolverBuilder customNamedResolverBuilder() {
        return new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byNamed",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }



    @Component
    @Qualifier("testStringQualifiedCustomResolverBuilderComponent")
    public static class CustomStringQualifiedResolverBuilderComponent extends PublicResolverBuilder{
        @Override
        protected boolean isQuery(Method method) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource", "byStringQualified",
                    "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

    @Component
    @TestComponentQualifier
    public static class CustomAnnotationQualifiedResolverBuilderComponent extends PublicResolverBuilder{
        @Override
        protected boolean isQuery(Method method) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource",
                    "byAnnotationQualified", "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

    @Component("testNamedCustomResolverBuilderComponent")
    public static class CustomNamedResolverBuilderComponent extends PublicResolverBuilder{
        @Override
        protected boolean isQuery(Method method) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource",
                    "byNamed", "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

}
