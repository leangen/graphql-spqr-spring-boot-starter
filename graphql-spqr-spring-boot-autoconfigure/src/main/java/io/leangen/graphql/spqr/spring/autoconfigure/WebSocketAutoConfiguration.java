package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.servlet.websocket.GraphQLWebSocketExecutor;
import io.leangen.graphql.spqr.spring.web.apollo.PerConnectionApolloHandler;
import io.leangen.graphql.spqr.spring.web.servlet.websocket.DefaultGraphQLExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableWebSocket
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnProperty(name = "graphql.spqr.ws.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class WebSocketAutoConfiguration implements WebSocketConfigurer {

    private final GraphQL graphQL;
    private final SpqrProperties config;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Autowired
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public WebSocketAutoConfiguration(GraphQL graphQL, SpqrProperties config,
                                      Optional<DataLoaderRegistryFactory> dataLoaderRegistryFactory) {
        this.graphQL = graphQL;
        this.config = config;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory.orElse(null);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        String webSocketEndpoint = config.getWs().getEndpoint();
        String graphQLEndpoint = config.getHttp().getEndpoint();
        String endpointUrl = webSocketEndpoint == null ? graphQLEndpoint : webSocketEndpoint;
        webSocketHandlerRegistry
                .addHandler(webSocketHandler(webSocketExecutor(webSocketContextFactory())), endpointUrl)
                .setAllowedOrigins(config.getWs().getAllowedOrigins());
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketContextFactory webSocketContextFactory() {
        return params -> new DefaultGlobalContext<>(params.getNativeRequest());
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQLWebSocketExecutor webSocketExecutor(WebSocketContextFactory contextFactory) {
        return new DefaultGraphQLExecutor(contextFactory, dataLoaderRegistryFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public PerConnectionApolloHandler webSocketHandler(GraphQLWebSocketExecutor executor) {
        boolean keepAliveEnabled = config.getWs().getKeepAlive().isEnabled();
        int keepAliveInterval = config.getWs().getKeepAlive().getIntervalMillis();
        return new PerConnectionApolloHandler(graphQL, executor,
                keepAliveEnabled ? defaultTaskScheduler() : null, keepAliveInterval);
    }

    private TaskScheduler defaultTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
        threadPoolScheduler.setThreadNamePrefix("GraphQLWSKeepAlive-");
        threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolScheduler.setRemoveOnCancelPolicy(true);
        threadPoolScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        threadPoolScheduler.initialize();
        return threadPoolScheduler;
    }
}
