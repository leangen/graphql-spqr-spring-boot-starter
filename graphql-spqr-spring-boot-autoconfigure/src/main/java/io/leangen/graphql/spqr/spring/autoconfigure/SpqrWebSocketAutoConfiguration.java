package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.spqr.spring.web.apollo.PerConnectionApolloHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableWebSocket
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnProperty(name = "graphql.spqr.ws.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class SpqrWebSocketAutoConfiguration implements WebSocketConfigurer {

    private final GraphQL graphQL;
    private final String webSocketEndpoint;
    private final String graphQLEndpoint;
    private final boolean keepAliveEnabled;
    private final int keepAliveInterval;

    @Autowired
    public SpqrWebSocketAutoConfiguration(
            GraphQL graphQL,
            @Value("${graphql.spqr.ws.endpoint:#{null}}") String webSocketEndpoint,
            @Value("${graphql.spqr.default-endpoint.mapping:/graphql}") String graphQLEndpoint,
            @Value("${graphql.spqr.ws.keepalive.enabled:false}") boolean keepAliveEnabled,
            @Value("${graphql.spqr.ws.keepalive.intervalMillis:10000}") int keepAliveInterval) {

        this.graphQL = graphQL;
        this.webSocketEndpoint = webSocketEndpoint;
        this.graphQLEndpoint = graphQLEndpoint;
        this.keepAliveEnabled = keepAliveEnabled;
        this.keepAliveInterval = keepAliveInterval;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        String endpointUrl = webSocketEndpoint == null ? graphQLEndpoint : webSocketEndpoint;
        webSocketHandlerRegistry.addHandler(webSocketHandler(), endpointUrl).setAllowedOrigins("*");
    }

    @Bean
    @ConditionalOnMissingBean
    public PerConnectionApolloHandler webSocketHandler() {
        return new PerConnectionApolloHandler(graphQL, keepAliveEnabled ? defaultTaskScheduler() : null, keepAliveInterval);
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
