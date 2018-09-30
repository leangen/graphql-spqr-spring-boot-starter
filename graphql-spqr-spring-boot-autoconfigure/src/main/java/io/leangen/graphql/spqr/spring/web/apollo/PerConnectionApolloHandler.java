package io.leangen.graphql.spqr.spring.web.apollo;

import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import java.util.Collections;
import java.util.List;

public class PerConnectionApolloHandler extends PerConnectionWebSocketHandler implements SubProtocolCapable {

    private static final List<String> GRAPHQL_WS = Collections.singletonList("graphql-ws");

    public PerConnectionApolloHandler() {
        super(ApolloProtocolHandler.class);
    }

    @Override
    public List<String> getSubProtocols() {
        return GRAPHQL_WS;
    }
}
