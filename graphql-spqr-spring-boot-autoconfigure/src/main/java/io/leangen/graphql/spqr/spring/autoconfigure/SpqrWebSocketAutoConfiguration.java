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
    private final SpqrProperties config;

    @Autowired
    public SpqrWebSocketAutoConfiguration(GraphQL graphQL, SpqrProperties config) {
        this.graphQL = graphQL;
        this.config = config;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        String webSocketEndpoint = config.getWs().getEndpoint();
        String graphQLEndpoint = config.getHttp().getEndpoint();
        String endpointUrl = webSocketEndpoint == null ? graphQLEndpoint : webSocketEndpoint;
        webSocketHandlerRegistry
                .addHandler(webSocketHandler(), endpointUrl)
                .setAllowedOrigins(config.getWs().getAllowedOrigins());
    }

    @Bean
    @ConditionalOnMissingBean
    public PerConnectionApolloHandler webSocketHandler() {
        boolean keepAliveEnabled = config.getWs().getKeepAlive().isEnabled();
        int keepAliveInterval = config.getWs().getKeepAlive().getIntervalMillis();
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
