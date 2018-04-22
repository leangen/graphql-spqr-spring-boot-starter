package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

@Configuration
@EnableWebSocket
@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnProperty(name = "graphql.spqr.websocket.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class SpqrWebSocketAutoConfiguration implements WebSocketConfigurer {

    @Value("${graphql.spqr.websocket.mapping:#{null}}")
    private String webSocketEndpoint;

    @Value("${graphql.spqr.default-endpoint.mapping:/graphql}")
    private String graphqlDefaultEndpoint;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        String endpointUrl = webSocketEndpoint == null ? graphqlDefaultEndpoint : webSocketEndpoint;
        webSocketHandlerRegistry.addHandler(webSocketHandler(), endpointUrl).setAllowedOrigins("*").withSockJS();
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new PerConnectionWebSocketHandler(SubscriptionWebSocketHandler.class);
    }
}
