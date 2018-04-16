package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

@EnableWebSocket
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "graphql.spqr.websocket.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(GraphQLSchema.class)
public class SpqrWebSocketAutoConfiguration implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSocketHandler(), "/graphql").setAllowedOrigins("*").withSockJS();
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new PerConnectionWebSocketHandler(SubscriptionWebSocketHandler.class);
    }
}
