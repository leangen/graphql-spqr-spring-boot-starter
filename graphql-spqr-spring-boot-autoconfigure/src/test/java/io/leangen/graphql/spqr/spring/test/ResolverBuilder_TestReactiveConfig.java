package io.leangen.graphql.spqr.spring.test;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class ResolverBuilder_TestReactiveConfig {

    @Component("annotatedOperationSourceReactiveBean")
    @GraphQLApi
    public static class AnnotatedOperationSourceReactiveBean {
        @GraphQLQuery(name = "greetingFromAnnotatedSourceReactive_mono")
        public Mono<String> getGreetingMono(){
            return Mono.just("Hello world !");
        }

        @GraphQLQuery(name = "greetingFromAnnotatedSourceReactive_flux")
        public Flux<String> getGreetingFlux(){
            return Flux.fromArray(new String[]{"First Hello world !","Second Hello world !"});
        }

        @GraphQLQuery(name = "echo")
        public Mono<String> echo(@GraphQLArgument(name = "content") String content) {
            return Mono.just(content);
        }
    }
}
