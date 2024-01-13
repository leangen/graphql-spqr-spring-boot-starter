package io.leangen.graphql.spqr.spring.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.leangen.graphql.ExtensionProvider;
import io.leangen.graphql.GeneratorConfiguration;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilderParams;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Configuration
public class ResolverBuilder_TestConfig {
    //------------------------------------------------------------------------------------------
    //------------ Global resolver builder config ----------------------------------------------
    //------------------------------------------------------------------------------------------
    @Bean
    public ExtensionProvider<GeneratorConfiguration, ResolverBuilder> globalResolverBuilderExtensionProvider() {
        return (config, defaults) -> defaults.insert(0, new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method, ResolverBuilderParams params) {
                final String[] expectedFragments = new String[] {
                        "byCustomGlobalResolverBuilder"};

                return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        });
    }

    //------------------------------------------------------------------------------------------
    //------------ Operation Sources -----------------------------------------------------------
    //------------------------------------------------------------------------------------------

    @Component("annotatedOperationSourceBean")
    @GraphQLApi
    public static class AnnotatedOperationSourceBean {
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsComponent")
        public String getGreeting() {
            return "Hello world !";
        }

        @GraphQLQuery(name = "echo")
        public String echo(@GraphQLArgument(name = "content") String content) {
            return content;
        }
    }

    @Bean
    @GraphQLApi
    public AnnotatedOperationSourceWired annotatedOperationSourceWired() {
        return new AnnotatedOperationSourceWired();
    }

    private static class AnnotatedOperationSourceWired {
        @GraphQLQuery(name = "greetingFromAnnotatedSource_wiredAsBean")
        public String getGreeting() {
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
        public String getGreeting() {
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
        public String getGreeting() {
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
    }

    @Component
    @GraphQLApi
    public static class SpringPageComponent {

        static List<User> users = IntStream.range(0, 20)
                .mapToObj(i -> new User(i + "id", "Duncan Idaho" + i, i + 10))
                .collect(Collectors.toList());


        static List<Project> projects = IntStream.range(0, 20)
                .boxed()
                .map(i -> new Project("Project"+i))
                .collect(Collectors.toList());


        @GraphQLQuery(name = "springPageComponent_users")
        public Page<User> users(@GraphQLArgument(name = "first") int first, @GraphQLArgument(name = "after") String after) {
            return new PageImpl<>(users.subList(0, first), PageRequest.of(0, first), users.size());
        }

        @GraphQLQuery(name = "springPageComponent_user_projects")
        public Page<Project> projects(@GraphQLContext User user, @GraphQLArgument(name = "first") int first, @GraphQLArgument(name = "after") String after) {
            return new PageImpl<>(projects.subList(0, first), PageRequest.of(0, first), users.size());
        }

        public static class User {
            private final String id;
            private final String name;
            private final Integer age;

            User(String id, String name, Integer age) {
                this.id = id;
                this.name = name;
                this.age = age;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public Integer getAge() {
                return age;
            }
        }

        public static class Project {
            private final String name;

            Project(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
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
            protected boolean isQuery(Method method, ResolverBuilderParams params) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byStringQualified",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }

    @Bean
    @TestBeanQualifier
    public ResolverBuilder customAnnotationQualifiedResolverBuilder() {
        return new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method, ResolverBuilderParams params) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byAnnotationQualified",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }

    @Bean(name = "testNamedCustomResolverBuilderBean")
    public ResolverBuilder customNamedResolverBuilder() {
        return new PublicResolverBuilder() {
            @Override
            protected boolean isQuery(Method method, ResolverBuilderParams params) {
                final String[] expectedFragments = new String[] {
                        "greetingFromBeanSource", "byNamed",
                        "ResolverBuilder_wiredAsBean"};

                return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
            }
        };
    }

    @Component
    @Qualifier("testStringQualifiedCustomResolverBuilderComponent")
    public static class CustomStringQualifiedResolverBuilderComponent extends PublicResolverBuilder {
        @Override
        protected boolean isQuery(Method method, ResolverBuilderParams params) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource", "byStringQualified",
                    "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

    @Component
    @TestComponentQualifier
    public static class CustomAnnotationQualifiedResolverBuilderComponent extends PublicResolverBuilder {
        @Override
        protected boolean isQuery(Method method, ResolverBuilderParams params) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource",
                    "byAnnotationQualified", "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

    @Component("testNamedCustomResolverBuilderComponent")
    public static class CustomNamedResolverBuilderComponent extends PublicResolverBuilder {
        @Override
        protected boolean isQuery(Method method, ResolverBuilderParams params) {
            final String[] expectedFragments = new String[] {
                    "greetingFromBeanSource",
                    "byNamed", "ResolverBuilder_wiredAsComponent"};

            return super.isQuery(method, params) && Arrays.stream(expectedFragments).parallel().allMatch(method.getName()::contains);
        }
    }

}
