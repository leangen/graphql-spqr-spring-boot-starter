package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.apollo.PerConnectionApolloHandler;
import io.leangen.graphql.spqr.spring.web.mvc.websocket.DefaultGraphQLExecutor;
import io.leangen.graphql.spqr.spring.web.mvc.websocket.GraphQLWebSocketExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

@AutoConfiguration
@EnableWebSocket
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnProperty(name = "graphql.spqr.ws.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class WebSocketAutoConfiguration {

    private final GraphQL graphQL;
    private final SpqrProperties config;
    private final DataLoaderRegistryFactory dataLoaderRegistryFactory;

    @Autowired
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    public WebSocketAutoConfiguration(GraphQL graphQL, SpqrProperties config,
                                      Optional<DataLoaderRegistryFactory> dataLoaderRegistryFactory) {
        this.graphQL = graphQL;
        this.config = config;
        this.dataLoaderRegistryFactory = dataLoaderRegistryFactory.orElse(null);
    }

    @Bean
    @ConditionalOnMissingBean(name = "spqrWebSocketConfigurer")
    public WebSocketConfigurer spqrWebSocketConfigurer(PerConnectionApolloHandler handler) {
        return webSocketHandlerRegistry -> webSocketHandlerRegistry
            .addHandler(handler, config.getWs().getEndpoint())
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
        int sendTimeLimit = config.getWs().getSendTimeLimit();
        int sendBufferSizeLimit = config.getWs().getSendBufferSizeLimit();
        return new PerConnectionApolloHandler(graphQL, executor,
                keepAliveEnabled ? defaultTaskScheduler() : null, keepAliveInterval,
                sendTimeLimit, sendBufferSizeLimit);
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
